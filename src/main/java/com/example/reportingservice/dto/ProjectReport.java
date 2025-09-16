package com.example.reportingservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectReport {
    private String name;
    private List<ProjectDailyHours> hoursSpent;
}
