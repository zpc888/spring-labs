package com.example.rest.logging.control;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DemoControllerTest {

    @Test
    void httpbinUsesErrorHandlerBodyAfterExecutingTheRequest() throws Exception {
        RestClient restClient = mock(RestClient.class);
        RestClient.RequestBodyUriSpec requestSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        AtomicReference<RestClient.ResponseSpec.ErrorHandler> errorHandlerRef = new AtomicReference<>();

        when(restClient.method(HttpMethod.GET)).thenReturn(requestSpec);
        when(requestSpec.uri("/status/404")).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        doAnswer(invocation -> {
            errorHandlerRef.set(invocation.getArgument(1));
            return responseSpec;
        }).when(responseSpec).onStatus(
                org.mockito.ArgumentMatchers.<Predicate<HttpStatusCode>>any(),
                any(RestClient.ResponseSpec.ErrorHandler.class));

        when(response.getBody()).thenReturn(new ByteArrayInputStream(new byte[0]));
        doAnswer(invocation -> {
            errorHandlerRef.get().handle(request, response);
            return null;
        }).when(responseSpec).body(String.class);

        DemoController controller = new DemoController(restClient);

        Map<String, Object> result = controller.httpbin("get", 404);

        assertEquals("[no error body]", result.get("get-404-response"));
    }
}
