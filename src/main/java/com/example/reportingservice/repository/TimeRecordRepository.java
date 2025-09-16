package com.example.reportingservice.repository;

import com.example.reportingservice.model.TimeRecord;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface TimeRecordRepository extends R2dbcRepository<TimeRecord, Long> {
    
    @Query("""
        SELECT DATE(tr.time_from) as day, p.name as project_name,
               SUM(EXTRACT(EPOCH FROM (tr.time_to - tr.time_from)) / 3600) as total_hours
        FROM time_record tr
        JOIN project p ON tr.project_id = p.id
        WHERE tr.employee_id = :employeeId
          AND (:startDate IS NULL OR tr.time_from >= :startDate)
          AND (:endDate IS NULL OR tr.time_to <= :endDate)
        GROUP BY DATE(tr.time_from), p.name
        ORDER BY DATE(tr.time_from), p.name
        """)
    Flux<EmployeeDailyHours> findEmployeeDailyHours(Long employeeId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("""
        SELECT DATE(tr.time_from) as day,
               SUM(EXTRACT(EPOCH FROM (tr.time_to - tr.time_from)) / 3600) as total_hours
        FROM time_record tr
        WHERE tr.project_id = :projectId
          AND (:startDate IS NULL OR tr.time_from >= :startDate)
          AND (:endDate IS NULL OR tr.time_to <= :endDate)
        GROUP BY DATE(tr.time_from)
        ORDER BY DATE(tr.time_from)
        """)
    Flux<ProjectDailyHoursData> findProjectDailyHours(Long projectId, LocalDateTime startDate, LocalDateTime endDate);
}
