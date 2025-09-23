package com.example.reportingservice.service;

import com.example.reportingservice.dto.*;
import com.example.reportingservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    
    private final TimeRecordRepository timeRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    
    public Flux<EmployeeReport> streamEmployeesReport(LocalDate startDate, LocalDate endDate) {
        log.info("Streaming employees report from {} to {}", startDate, endDate);
        
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        
        return employeeRepository.findAll()
                .concatMap(employee -> 
                    timeRecordRepository.findEmployeeDailyHours(employee.getId(), startDateTime, endDateTime)
                        .map(dailyHours -> new DailyHours(dailyHours.getDay(), dailyHours.getProjectName(), dailyHours.getTotalHours()))
                        .collectList()
                        .map(hoursSpent -> new EmployeeReport(employee.getName(), hoursSpent))
                )
                .doOnNext(employeeReport -> log.debug("Streaming employee report for: {}", employeeReport.getName()))
                .doOnError(error -> log.error("Error streaming employees report", error));
    }
    
    public Flux<ProjectReport> streamProjectsReport(LocalDate startDate, LocalDate endDate) {
        log.info("Streaming projects report from {} to {}", startDate, endDate);
        
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        
        return projectRepository.findAll()
                .concatMap(project -> 
                    timeRecordRepository.findProjectDailyHours(project.getId(), startDateTime, endDateTime)
                        .map(dailyHours -> new ProjectDailyHours(dailyHours.getDay(), dailyHours.getTotalHours()))
                        .collectList()
                        .map(hoursSpent -> new ProjectReport(project.getName(), hoursSpent))
                )
                .doOnNext(projectReport -> log.debug("Streaming project report for: {}", projectReport.getName()))
                .doOnError(error -> log.error("Error streaming projects report", error));
    }
}
