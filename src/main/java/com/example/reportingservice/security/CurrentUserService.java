package com.example.reportingservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
public class CurrentUserService {

    // Demo mapping: username -> employeeId
    private static final Map<String, Long> USER_TO_EMPLOYEE_MAP = Map.of(
            "user", 101L,  // user can see data for employee ID 101 (Tom)
            "admin", 999L // admin can see all data (this mapping won't be used)
    );

    public Mono<String> getCurrentUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName);
    }

    public Mono<Long> getCurrentEmployeeId() {
        return getCurrentUsername()
                .map(username -> {
                    Long employeeId = USER_TO_EMPLOYEE_MAP.get(username);
                    if (employeeId == null) {
                        throw new IllegalStateException("No employee mapping found for user: " + username);
                    }
                    return employeeId;
                })
                .doOnNext(employeeId -> log.debug("Mapped to employee ID: {}", employeeId));
    }

    public Mono<Boolean> hasRole(String role) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role)))
                .doOnNext(hasRole -> log.debug("User has role {}: {}", role, hasRole));
    }

    public Mono<Boolean> isAdmin() {
        return hasRole("ADMIN");
    }
}
