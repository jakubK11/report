package com.example.reportingservice.controller;

import com.example.reportingservice.dto.EmployeeReport;
import com.example.reportingservice.dto.ProjectReport;
import com.example.reportingservice.exception.InvalidDateRangeException;
import com.example.reportingservice.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @Test
    void whenDatesAreInvalid_forEmployeesReport_thenThrowsException() {
        LocalDate startDate = LocalDate.of(2023, 1, 10);
        LocalDate endDate = LocalDate.of(2023, 1, 1);

        assertThrows(InvalidDateRangeException.class, () -> {
            reportController.streamEmployeesReport(startDate, endDate);
        });

        verify(reportService, never()).streamEmployeesReport(any(), any());
    }

    @Test
    void whenDatesAreValid_forEmployeesReport_thenReturnsFlux() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 10);
        when(reportService.streamEmployeesReport(startDate, endDate)).thenReturn(Flux.just(new EmployeeReport()));

        Flux<EmployeeReport> result = reportController.streamEmployeesReport(startDate, endDate);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
        verify(reportService).streamEmployeesReport(startDate, endDate);
    }

    @Test
    void whenDatesAreInvalid_forProjectsReport_thenThrowsException() {
        LocalDate startDate = LocalDate.of(2023, 1, 10);
        LocalDate endDate = LocalDate.of(2023, 1, 1);

        assertThrows(InvalidDateRangeException.class, () -> {
            reportController.streamProjectsReport(startDate, endDate);
        });

        verify(reportService, never()).streamProjectsReport(any(), any());
    }

    @Test
    void whenDatesAreValid_forProjectsReport_thenReturnsFlux() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 10);
        when(reportService.streamProjectsReport(startDate, endDate)).thenReturn(Flux.just(new ProjectReport()));

        Flux<ProjectReport> result = reportController.streamProjectsReport(startDate, endDate);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
        verify(reportService).streamProjectsReport(startDate, endDate);
    }
}
