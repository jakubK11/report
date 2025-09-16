package com.example.reportingservice.repository;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDailyHours {
    private LocalDate day;
    private String projectName;
    private BigDecimal totalHours;
}
