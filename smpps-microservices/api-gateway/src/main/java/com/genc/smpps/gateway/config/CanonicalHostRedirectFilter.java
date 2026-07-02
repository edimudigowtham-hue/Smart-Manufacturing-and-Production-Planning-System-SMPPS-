package com.genc.smpps.gateway.config;

import java.net.URI;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CanonicalHostRedirectFilter implements WebFilter {
    private static final String CANONICAL_BASE_URL = "http://localhost:8889";

    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String host = exchange.getRequest().getURI().getHost();
        if (host == null || isLocalhost(host)) {
            return chain.filter(exchange);
        }

        URI requestUri = exchange.getRequest().getURI();
        String path = requestUri.getRawPath() == null || requestUri.getRawPath().isBlank()
                ? "/"
                : requestUri.getRawPath();
        String query = requestUri.getRawQuery();
        URI redirectUri = URI.create(CANONICAL_BASE_URL + path + (query == null ? "" : "?" + query));

        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
        response.getHeaders().set(HttpHeaders.LOCATION, redirectUri.toString());
        return response.setComplete();
    }

    private boolean isLocalhost(String host) {
        return "localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host)
                || "::1".equals(host)
                || "0:0:0:0:0:0:0:1".equals(host);
    }
}


