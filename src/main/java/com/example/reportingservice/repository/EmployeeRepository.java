package com.example.reportingservice.repository;

import com.example.reportingservice.model.Employee;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends R2dbcRepository<Employee, Long> {
    
}
