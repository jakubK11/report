package com.example.reportingservice.repository;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDailyHoursData {
    private LocalDate day;
    private BigDecimal totalHours;
}
