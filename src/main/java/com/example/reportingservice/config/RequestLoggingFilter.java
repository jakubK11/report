package com.example.reportingservice.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.example.reportingservice.security.CurrentUserService;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
@Order(100)  // Run after security filters to capture authenticated user
@Slf4j
public class RequestLoggingFilter implements WebFilter {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    CurrentUserService currentUserService;

    public RequestLoggingFilter(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        Instant startTime = Instant.now();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String queryParams = exchange.getRequest().getQueryParams().toString();
        
        return currentUserService.getCurrentUsername()
                .defaultIfEmpty("anonymous")
                .flatMap(username -> 
                    chain.filter(exchange)
                            .doOnSuccess(aVoid -> {
                                org.springframework.http.HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
                                int status = statusCode != null ? statusCode.value() : 200;
                                logRequestCompletion(method, path, queryParams, startTime, status, null, username);
                            })
                            .doOnError(throwable -> 
                                logRequestCompletion(method, path, queryParams, startTime, 500, throwable, username)
                            )
                );
    }

    private void logRequestCompletion(String method, String path, String queryParams, 
                                    Instant startTime, int statusCode, Throwable error, String username) {
        Duration duration = Duration.between(startTime, Instant.now());
        
        // Audit log with user information
        AUDIT.info("type=http method={} path={} query={} status={} durationMs={} user={} error={}", 
            method, path, queryParams, statusCode, duration.toMillis(), username,
            error != null ? error.getMessage() : null);
        
        // Regular log
        if (error != null) {
            log.error("Request completed: {} {} {} | User: {} | Status: {} | Duration: {}ms | Error: {}", 
                method, path, queryParams, username, statusCode, duration.toMillis(), error.getMessage());
        } else {
            log.info("Request completed: {} {} {} | User: {} | Status: {} | Duration: {}ms", 
                method, path, queryParams, username, statusCode, duration.toMillis());
        }
    }
}
