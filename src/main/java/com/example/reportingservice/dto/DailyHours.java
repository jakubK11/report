package com.example.reportingservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class DailyHours {
    private LocalDate day;
    private String projectName;
    private BigDecimal totalHours;
    
    public DailyHours(LocalDate day, String projectName, BigDecimal totalHours) {
        this.day = day;
        this.projectName = projectName;
        this.totalHours = totalHours != null ? totalHours.setScale(2, RoundingMode.HALF_UP) : null;
    }
    
    public void setTotalHours(BigDecimal totalHours) {
        this.totalHours = totalHours != null ? totalHours.setScale(2, RoundingMode.HALF_UP) : null;
    }
}
