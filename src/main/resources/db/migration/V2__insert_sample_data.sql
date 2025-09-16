-- Sample data insertion
INSERT INTO employee (id, name) VALUES 
    (101, 'Tom'),
    (102, 'Jerry');

INSERT INTO project (id, name) VALUES 
    (1, 'Sample Project A'),
    (2, 'Sample Project B');

INSERT INTO time_record (id, employee_id, project_id, time_from, time_to) VALUES
    (1, 101, 1, '2024-02-01 08:00:00', '2024-02-01 17:00:00'),
    (2, 102, 2, '2024-02-01 09:00:00', '2024-02-01 18:30:00'),
    (3, 101, 1, '2024-02-02 08:15:00', '2024-02-02 17:10:00');
