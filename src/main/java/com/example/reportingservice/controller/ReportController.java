package com.example.reportingservice.controller;

import com.example.reportingservice.dto.EmployeeReport;
import com.example.reportingservice.dto.ProjectReport;
import com.example.reportingservice.exception.InvalidDateRangeException;
import com.example.reportingservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    @GetMapping(value = "/report/employees", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<EmployeeReport> streamEmployeesReport(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate startDate,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate endDate) {
        
        log.info("Streaming employees report with startDate: {}, endDate: {}", startDate, endDate);
        validateDates(startDate, endDate);
        return reportService.streamEmployeesReport(startDate, endDate);
    }
    
    @GetMapping(value = "/report/projects", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<ProjectReport> streamProjectsReport(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate startDate,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate endDate) {
        
        log.info("Streaming projects report with startDate: {}, endDate: {}", startDate, endDate);
        validateDates(startDate, endDate);
        return reportService.streamProjectsReport(startDate, endDate);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidDateRangeException("End date cannot be before start date.");
        }
    }
}
