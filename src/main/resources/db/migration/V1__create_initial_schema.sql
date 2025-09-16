-- Initial schema creation
DROP TABLE IF EXISTS time_record;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS employee;

CREATE TABLE project (
    id BIGINT PRIMARY KEY,
    name VARCHAR(200) NOT NULL
);

CREATE TABLE employee (
    id BIGINT PRIMARY KEY,
    name VARCHAR(60) NOT NULL
);

CREATE TABLE time_record (
    id BIGINT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    time_from TIMESTAMP NOT NULL,
    time_to TIMESTAMP NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employee(id),
    FOREIGN KEY (project_id) REFERENCES project(id)
);
