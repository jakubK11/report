package com.example.reportingservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ProjectDailyHours {
    private LocalDate day;
    private BigDecimal totalHours;
    
    public ProjectDailyHours(LocalDate day, BigDecimal totalHours) {
        this.day = day;
        this.totalHours = totalHours != null ? totalHours.setScale(2, RoundingMode.HALF_UP) : null;
    }
    
    public void setTotalHours(BigDecimal totalHours) {
        this.totalHours = totalHours != null ? totalHours.setScale(2, RoundingMode.HALF_UP) : null;
    }
}
