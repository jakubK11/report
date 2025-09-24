package com.example.reportingservice.service;

import com.example.reportingservice.dto.*;
import com.example.reportingservice.model.Employee;
import com.example.reportingservice.repository.*;
import com.example.reportingservice.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    
    private final TimeRecordRepository timeRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;
    
    public Flux<EmployeeReport> streamEmployeesReport(LocalDate startDate, LocalDate endDate) {
        log.info("Streaming employees report from {} to {}", startDate, endDate);
        
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        
        return currentUserService.isAdmin()
                .flatMapMany(isAdmin -> {
                    if (Boolean.TRUE.equals(isAdmin)) {
                        // ADMIN: fetch all employees from database
                        log.debug("Admin user - fetching all employees from database");
                        return employeeRepository.findAll();
                    } else {
                        // USER: fetch only the specific employee from database
                        return findAndValidateEmployee();
                    }
                })
                .concatMap(employee -> 
                    timeRecordRepository.findEmployeeDailyHours(employee.getId(), startDateTime, endDateTime)
                        .map(dailyHours -> new DailyHours(dailyHours.getDay(), dailyHours.getProjectName(), dailyHours.getTotalHours()))
                        .collectList()
                        .map(hoursSpent -> new EmployeeReport(employee.getName(), hoursSpent))
                )
                .doOnNext(employeeReport -> log.debug("Streaming employee report for: {}", employeeReport.getName()))
                .doOnError(error -> log.error("Error streaming employees report", error));
    }

    private Flux<Employee> findAndValidateEmployee() {
        return currentUserService.getCurrentEmployeeId()
                .flatMapMany(employeeId -> {
                    log.debug("Regular user - fetching employee ID {} from database", employeeId);
                    return employeeRepository.findById(employeeId)
                            .switchIfEmpty(Mono.error(new IllegalStateException(
                                "Employee with ID " + employeeId + " not found in database")));
                });
    }
    
    public Flux<ProjectReport> streamProjectsReport(LocalDate startDate, LocalDate endDate) {
        log.info("Streaming projects report from {} to {}", startDate, endDate);
        
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        
        return currentUserService.isAdmin()
                .flatMapMany(isAdmin -> {
                    if (Boolean.TRUE.equals(isAdmin)) {
                        // ADMIN: get all projects with no employee filter
                        log.debug("Admin user - fetching all projects");
                        return createProjectReport(startDateTime, endDateTime, null);
                    } else {
                        // USER: get only projects they worked on
                        return findAndValidateEmployee()
                                .flatMap(employee -> 
                                    createProjectReport(startDateTime, endDateTime, employee.getId())
                                );
                    }
                })
                .doOnNext(projectReport -> log.debug("Streaming project report for: {}", projectReport.getName()))
                .doOnError(error -> log.error("Error streaming projects report", error));
    }

    private Flux<ProjectReport> createProjectReport(LocalDateTime startDateTime, LocalDateTime endDateTime, Long employeeId) {
        return projectRepository.findAll()
                .concatMap(project -> 
                    timeRecordRepository.findProjectDailyHours(project.getId(), employeeId, startDateTime, endDateTime)
                        .map(dailyHours -> new ProjectDailyHours(dailyHours.getDay(), dailyHours.getTotalHours()))
                        .collectList()
                        .filter(hoursSpent -> !hoursSpent.isEmpty()) // Only include projects with data
                        .map(hoursSpent -> new ProjectReport(project.getName(), hoursSpent))
                );
    }
}
