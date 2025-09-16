package com.example.reportingservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("time_record")
public class TimeRecord {
    @Id
    private Long id;
    private Long employeeId;
    private Long projectId;
    private LocalDateTime timeFrom;
    private LocalDateTime timeTo;
}
