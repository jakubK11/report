package com.example.reportingservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
@Order(-1)
@Slf4j
public class RequestLoggingFilter implements WebFilter {

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        Instant startTime = Instant.now();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String queryParams = exchange.getRequest().getQueryParams().toString();
        
        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    org.springframework.http.HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
                    int status = statusCode != null ? statusCode.value() : 200;
                    logRequestCompletion(method, path, queryParams, startTime, status, null);
                })
                .doOnError(throwable -> logRequestCompletion(method, path, queryParams, startTime, 
                    500, throwable))
                .onErrorResume(Mono::error);
    }

    private void logRequestCompletion(String method, String path, String queryParams, 
                                    Instant startTime, int statusCode, Throwable error) {
        Duration duration = Duration.between(startTime, Instant.now());
        
        if (error != null) {
            log.error("Request completed: {} {} {} | Status: {} | Duration: {}ms | Error: {}", 
                method, path, queryParams, statusCode, duration.toMillis(), error.getMessage());
        } else {
            log.info("Request completed: {} {} {} | Status: {} | Duration: {}ms", 
                method, path, queryParams, statusCode, duration.toMillis());
        }
    }
}
