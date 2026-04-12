package com.example.gateway.predicate;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Component
public class TenantHeaderRoutePredicateFactory
        extends AbstractRoutePredicateFactory<TenantHeaderRoutePredicateFactory.Config> {

    public TenantHeaderRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("tenant");
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();
            String tenantHeader = request.getHeaders().getFirst("X-Tenant");
            return StringUtils.hasText(config.getTenant())
                    && StringUtils.hasText(tenantHeader)
                    && tenantHeader.equalsIgnoreCase(config.getTenant());
        };
    }

    public static class Config {
        private String tenant;

        public String getTenant() {
            return tenant;
        }

        public void setTenant(String tenant) {
            this.tenant = tenant;
        }
    }
}

