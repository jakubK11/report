package com.example.reportingservice.repository;

import com.example.reportingservice.model.Project;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends R2dbcRepository<Project, Long> {
    
}
