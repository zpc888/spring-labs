package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class RequestTimingGatewayFilterFactory
		extends AbstractGatewayFilterFactory<RequestTimingGatewayFilterFactory.Config> {

	private static final String DEFAULT_HEADER_NAME = "X-Response-Time-Ms";

	public RequestTimingGatewayFilterFactory() {
		super(Config.class);
	}

	@Override
	public List<String> shortcutFieldOrder() {
		return Collections.singletonList("headerName");
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			long startNanos = System.nanoTime();

			exchange.getResponse().beforeCommit(() -> {
				long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
				String headerName = config.getHeaderName();
				if (headerName == null || headerName.isBlank()) {
					headerName = DEFAULT_HEADER_NAME;
				}
				exchange.getResponse().getHeaders().set(headerName, String.valueOf(durationMs));
				return Mono.empty();
			});

			return chain.filter(exchange);
		};
	}

	public static class Config {
		private String headerName = DEFAULT_HEADER_NAME;

		public String getHeaderName() {
			return headerName;
		}

		public void setHeaderName(String headerName) {
			this.headerName = headerName;
		}
	}
}

