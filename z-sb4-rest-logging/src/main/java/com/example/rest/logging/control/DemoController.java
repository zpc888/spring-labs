package com.example.rest.logging.control;

import com.example.rest.logging.observability.DemoObservability;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

@RestController
@RequestMapping("/api")
public class DemoController {

    private final RestClient restClient;
    private final DemoObservability observability;

    public DemoController(RestClient restClient, DemoObservability observability) {
        this.restClient = restClient;
        this.observability = observability;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> credentials) {
        return observability.observe("inbound", "login", () -> Map.of(
                "status", "authenticated",
                "token", "******"
        ));
    }

    @GetMapping("/httpbin/status/{httpMethod}/{status}")
    public Map<String, Object> httpbin(@PathVariable String httpMethod, @PathVariable int status) {
        return observability.observe("inbound", "httpbin", () -> {
            RestClient.ResponseSpec respSpec = restClient
                    .method(HttpMethod.valueOf(httpMethod.toUpperCase()))
                    .uri("/status/" + status)
                    .retrieve();
            String body = getBody(respSpec);
            return Map.of(
                    String.format("%s-%s-response", httpMethod, status), body
            );
        });
    }

    @PostMapping("/httpbin/batch")
    public List<Map<String, Object>> httpbinBatch(@RequestBody List<ReqModel> requests) {
        return observability.observe("inbound", "httpbin-batch", () -> {
            observability.recordBatchSize("httpbin-batch", requests.size());

            List<Map<String, Object>> ret = new ArrayList<>();
            for (ReqModel reqModel : requests) {
                ret.add(executeRequest(reqModel));
            }
            return ret;
        });
    }

    @PostMapping("/httpbin/parallel-batch")
    public List<Map<String, Object>> httpbinParallelBatch(@RequestBody List<ReqModel> requests) {
        return observability.observe("inbound", "httpbin-parallel-batch", () -> {
            observability.recordBatchSize("httpbin-parallel-batch", requests.size());

            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                List<StructuredTaskScope.Subtask<Map<String, Object>>> subtasks = requests.stream()
                        .map(req -> scope.fork(() -> executeRequest(req)))
                        .toList();

                scope.join().throwIfFailed();

                return subtasks.stream()
                        .map(StructuredTaskScope.Subtask::get)
                        .toList();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Parallel batch interrupted", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("One or more requests failed", e.getCause());
            }
        });
    }

    private Map<String, Object> executeRequest(ReqModel reqModel) {
        return observability.observe("outbound", reqModel.getMethod().toLowerCase(), () -> {
            RestClient.RequestBodySpec reqBodySpec = restClient
                    .method(HttpMethod.valueOf(reqModel.getMethod().toUpperCase()))
                    .uri(reqModel::resolveUri);
            if (reqModel.getHeaders() != null) {
                for (Map.Entry<String, String> header : reqModel.getHeaders().entrySet()) {
                    reqBodySpec.header(header.getKey(), header.getValue());
                }
            }
            if (reqModel.getBody() != null) {
                reqBodySpec.contentType(MediaType.APPLICATION_JSON)
                        .body(reqModel.getBody());
            }
            String body = getBody(reqBodySpec.retrieve());
            return Map.of(
                    String.format("%s-%s-response", reqModel.getMethod(), reqModel.getUri()), body
            );
        });
    }

    static class ReqModel {
        private String method;
        private String uri;
        private Map<String, Object> queryParms;
        private Map<String, String> headers;
        private Object body;

        public URI resolveUri(UriBuilder uriBuilder) {
            uriBuilder.path(uri);
            if (queryParms != null && !queryParms.isEmpty()) {
                for (String key : queryParms.keySet()) {
                    Object val = queryParms.get(key);
                    if (val instanceof Collection c) {
                        for (Object o : c) {
                            uriBuilder.queryParam(key, o);
                        }
                    } else {
                        uriBuilder.queryParam(key, val);
                    }
                }
            }
            return uriBuilder.build();
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public Map<String, Object> getQueryParms() {
            return queryParms;
        }

        public void setQueryParms(Map<String, Object> queryParms) {
            this.queryParms = queryParms;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public Object getBody() {
            return body;
        }

        public void setBody(Object body) {
            this.body = body;
        }
    }

    private static @NonNull String getBody(RestClient.ResponseSpec respSpec) {
        HttpClientErrorHandler errorHandler = new HttpClientErrorHandler();
        respSpec.onStatus(httpStatus -> !httpStatus.is2xxSuccessful(), errorHandler);
        String body = respSpec.body(String.class);
        if (body == null) {
            body = errorHandler.getBody();
        }
        if (body == null) {
            body = errorHandler.isInvoked() ? "[null error body]" : "[null ok body]";
        } else if (body.isEmpty()) {
            body = errorHandler.isInvoked() ? "[empty error body]" : "[empty ok body]";
        }
        return body;
    }

    static class HttpClientErrorHandler implements RestClient.ResponseSpec.ErrorHandler {
        private String body = null;
        private boolean invoked = false;

        @Override
        public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {
            body = new String(response.getBody().readAllBytes());
            invoked = true;
        }

        public String getBody() {
            return body;
        }

        public boolean isInvoked() {
            return invoked;
        }
    }
}
