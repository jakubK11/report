package com.example.reportingservice.integration;

import com.example.reportingservice.dto.EmployeeReport;
import com.example.reportingservice.dto.ProjectReport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class ReportControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("reporting_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // R2DBC configuration - matches main application.yml format
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/" + postgres.getDatabaseName());
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        
        // Flyway configuration - matches main application.yml format exactly
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldStreamEmployeesReportWithNDJSON() {
        // When: Call the employees report endpoint
        Flux<EmployeeReport> result = webTestClient
                .get()
                .uri("/api/v1/report/employees")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_NDJSON)
                .returnResult(EmployeeReport.class)
                .getResponseBody();

        // Then: Verify the streaming response
        StepVerifier.create(result)
                .assertNext(employeeReport -> {
                    assertThat(employeeReport.getName()).isIn("Tom", "Jerry");
                    assertThat(employeeReport.getHoursSpent()).isNotEmpty();
                    
                    if ("Tom".equals(employeeReport.getName())) {
                        // Tom has 2 time records: 9 hours on 2024-02-01 and ~8.92 hours on 2024-02-02
                        assertThat(employeeReport.getHoursSpent()).hasSize(2);
                        assertThat(employeeReport.getHoursSpent().get(0).getTotalHours().compareTo(new BigDecimal("9.0"))).isEqualTo(0);
                        assertThat(employeeReport.getHoursSpent().get(0).getProjectName()).isEqualTo("Sample Project A");
                    } else if ("Jerry".equals(employeeReport.getName())) {
                        // Jerry has 1 time record: 9.5 hours on 2024-02-01
                        assertThat(employeeReport.getHoursSpent()).hasSize(1);
                        assertThat(employeeReport.getHoursSpent().get(0).getTotalHours().compareTo(new BigDecimal("9.5"))).isEqualTo(0);
                        assertThat(employeeReport.getHoursSpent().get(0).getProjectName()).isEqualTo("Sample Project B");
                    }
                })
                .assertNext(employeeReport -> {
                    assertThat(employeeReport.getName()).isIn("Tom", "Jerry");
                    assertThat(employeeReport.getHoursSpent()).isNotEmpty();
                })
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    void shouldStreamEmployeesReportWithDateFilter() {
        // When: Call the employees report endpoint with date filter
        Flux<EmployeeReport> result = webTestClient
                .get()
                .uri("/api/v1/report/employees?startDate=2024-02-01&endDate=2024-02-01")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_NDJSON)
                .returnResult(EmployeeReport.class)
                .getResponseBody();

        // Then: Verify only records from 2024-02-01 are included
        StepVerifier.create(result)
                .assertNext(employeeReport -> {
                    if ("Tom".equals(employeeReport.getName())) {
                        // Tom should have only 1 record for 2024-02-01
                        assertThat(employeeReport.getHoursSpent()).hasSize(1);
                        assertThat(employeeReport.getHoursSpent().get(0).getDay().toString()).isEqualTo("2024-02-01");
                    } else if ("Jerry".equals(employeeReport.getName())) {
                        // Jerry should have 1 record for 2024-02-01
                        assertThat(employeeReport.getHoursSpent()).hasSize(1);
                        assertThat(employeeReport.getHoursSpent().get(0).getDay().toString()).isEqualTo("2024-02-01");
                    }
                })
                .assertNext(employeeReport -> {
                    assertThat(employeeReport.getHoursSpent()).allMatch(hours -> 
                        hours.getDay().toString().equals("2024-02-01"));
                })
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    void shouldStreamProjectsReportWithNDJSON() {
        // When: Call the projects report endpoint
        Flux<ProjectReport> result = webTestClient
                .get()
                .uri("/api/v1/report/projects")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_NDJSON)
                .returnResult(ProjectReport.class)
                .getResponseBody();

        // Then: Verify the streaming response
        StepVerifier.create(result)
                .assertNext(projectReport -> {
                    assertThat(projectReport.getName()).isIn("Sample Project A", "Sample Project B");
                    assertThat(projectReport.getHoursSpent()).isNotEmpty();
                    
                    if ("Sample Project A".equals(projectReport.getName())) {
                        // Project A has Tom's 2 time records
                        assertThat(projectReport.getHoursSpent()).hasSize(2);
                        // Should have records for 2024-02-01 and 2024-02-02
                        assertThat(projectReport.getHoursSpent().get(0).getTotalHours().compareTo(new BigDecimal("9.0"))).isEqualTo(0);
                    } else if ("Sample Project B".equals(projectReport.getName())) {
                        // Project B has Jerry's 1 time record
                        assertThat(projectReport.getHoursSpent()).hasSize(1);
                        assertThat(projectReport.getHoursSpent().get(0).getTotalHours().compareTo(new BigDecimal("9.5"))).isEqualTo(0);
                    }
                })
                .assertNext(projectReport -> {
                    assertThat(projectReport.getName()).isIn("Sample Project A", "Sample Project B");
                    assertThat(projectReport.getHoursSpent()).isNotEmpty();
                })
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    void shouldStreamProjectsReportWithDateFilter() {
        // When: Call the projects report endpoint with date filter
        Flux<ProjectReport> result = webTestClient
                .get()
                .uri("/api/v1/report/projects?startDate=2024-02-02&endDate=2024-02-02")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_NDJSON)
                .returnResult(ProjectReport.class)
                .getResponseBody();

        // Then: Verify only records from 2024-02-02 are included
        StepVerifier.create(result)
                .assertNext(projectReport -> {
                    if ("Sample Project A".equals(projectReport.getName())) {
                        // Sample Project A should have records on 2024-02-02 (Tom's second record)
                        assertThat(projectReport.getHoursSpent()).hasSize(1);
                        assertThat(projectReport.getHoursSpent().get(0).getDay().toString()).isEqualTo("2024-02-02");
                    } else if ("Sample Project B".equals(projectReport.getName())) {
                        // Sample Project B should have no records for 2024-02-02
                        assertThat(projectReport.getHoursSpent()).isEmpty();
                    }
                })
                .assertNext(projectReport -> {
                    // Second project (the other one)
                    if ("Sample Project A".equals(projectReport.getName())) {
                        assertThat(projectReport.getHoursSpent()).hasSize(1);
                        assertThat(projectReport.getHoursSpent().get(0).getDay().toString()).isEqualTo("2024-02-02");
                    } else if ("Sample Project B".equals(projectReport.getName())) {
                        assertThat(projectReport.getHoursSpent()).isEmpty();
                    }
                })
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    void shouldHandleEmptyResultsGracefully() {
        // When: Call with date range that has no data
        Flux<EmployeeReport> result = webTestClient
                .get()
                .uri("/api/v1/report/employees?startDate=2025-01-01&endDate=2025-01-31")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_NDJSON)
                .returnResult(EmployeeReport.class)
                .getResponseBody();

        // Then: Should complete without any records
        StepVerifier.create(result)
                .expectNextCount(2) // Still returns employees but with empty hours
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    void whenDateRangeIsInvalid_shouldReturnBadRequest() {
        // Given an invalid date range
        LocalDate startDate = LocalDate.of(2024, 2, 2);
        LocalDate endDate = LocalDate.of(2024, 2, 1);

        // When: Call the endpoint with the invalid range
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/report/employees")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .build())
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                // Then: Expect a 400 Bad Request response
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("End date cannot be before start date.");
    }
}
