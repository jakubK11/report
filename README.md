# Reporting Service

A reactive Spring Boot microservice for generating reports, built with WebFlux and R2DBC for PostgreSQL.

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Git

## Development Environment Setup

### Quick Start with Hot Reload (Recommended)

**Option A: One-Command Start (Easiest)**
```bash
# Start everything with one command
$ ./dev-start.sh
```
This script will:
- Start PostgreSQL container
- Wait for it to be ready
- Start the Spring Boot app with hot reload
- On Ctrl-C: stops only the app, PostgreSQL keeps running

**Option B: Manual Start**
```bash
# Start PostgreSQL
$ docker-compose -f docker-compose.dev.yml up -d

# Run the application with local profile (includes hot reload)
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Available Commands

```bash
# One-command development start
$ ./dev-start.sh

# Stop everything (PostgreSQL + app)
$ ./dev-stop.sh

# Manual database commands
$ docker-compose -f docker-compose.dev.yml up -d    # Start database only
$ docker-compose -f docker-compose.dev.yml down     # Stop database

# Manual app commands
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=local  # Run with hot reload

# Build and test
$ ./mvnw clean install
$ ./mvnw test
```

**Note**: You'll need to make the scripts executable first:
```bash
$ chmod +x dev-start.sh dev-stop.sh
```

## Configuration

### Application Profiles
- **local**: Local development with hot reload (connects to localhost:5432)
- **docker**: Containerized environment (connects to postgres:5432)

### Database Configuration
- **Host**: localhost
- **Port**: 5432
- **Database**: reporting_db
- **Username**: postgres
- **Password**: postgres

## Security and Authentication

- Basic authentication is enabled for the API.
- Public endpoint: `GET /actuator/health`
- Authentication required: all paths under `/api/**`
- Demo users defined in `src/main/java/com/example/reportingservice/config/SecurityConfig.java`:
  - admin / admin123 — role: `ADMIN`
  - user / user123 — role: `USER`

Example curl usage with Basic Auth:
```bash
# Health check (no auth required)
curl http://localhost:8080/actuator/health

# Employees report (auth required)
curl -u user:user123 "http://localhost:8080/api/v1/report/employees"

# Projects report (auth required)
curl -u user:user123 "http://localhost:8080/api/v1/report/projects"
```

## API Endpoints

### Employee Reports
- `GET /api/v1/report/employees` - Generate paginated employee work hours report

**Parameters:**
- `startDate` (optional): Start date filter (ISO format: YYYY-MM-DD)
- `endDate` (optional): End date filter (ISO format: YYYY-MM-DD)  
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size

**Examples:**
```bash

 $ curl -u user:user123 "http://localhost:8080/api/v1/report/employees"

# Stream employees with date filtering
 $ curl -u user:user123 "http://localhost:8080/api/v1/report/employees?startDate=2024-02-01&endDate=2024-02-02"

```

**Response Format:** Newline-Delimited JSON (NDJSON) - each employee record on a separate line:
```
{"name":"Tom","hoursSpent":[{"day":"2024-02-01","projectName":"Sample Project A","totalHours":9.0}]}
{"name":"Jerry","hoursSpent":[{"day":"2024-02-01","projectName":"Sample Project B","totalHours":9.5}]}
```

### Project Reports
- `GET /api/v1/report/projects` - Generate paginated project work hours report

**Parameters:**
- `startDate` (optional): Start date filter (ISO format: YYYY-MM-DD)
- `endDate` (optional): End date filter (ISO format: YYYY-MM-DD)
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size

**Examples:**
```bash
 $ curl -u user:user123 "http://localhost:8080/api/v1/report/projects"

# Stream projects for specific date range
 $ curl -u user:user123 "http://localhost:8080/api/v1/report/projects?startDate=2024-02-01&endDate=2024-02-02"
```

**Response Format:** Newline-Delimited JSON (NDJSON) - each project record on a separate line:
```
{"name":"Sample Project A","hoursSpent":[{"day":"2024-02-01","totalHours":9.0}]}
{"name":"Sample Project B","hoursSpent":[{"day":"2024-02-01","totalHours":9.5}]}
```

## Streaming Response Format

Both endpoints return **NDJSON (Newline-Delimited JSON)** for true streaming:

### Employee Report Stream
Each line contains one employee record:
```
{"name":"Tom","hoursSpent":[{"day":"2024-02-01","projectName":"Sample Project A","totalHours":9.0},{"day":"2024-02-02","projectName":"Sample Project A","totalHours":8.92}]}
{"name":"Jerry","hoursSpent":[{"day":"2024-02-01","projectName":"Sample Project B","totalHours":9.5}]}
```

### Project Report Stream  
Each line contains one project record:
```
{"name":"Sample Project A","hoursSpent":[{"day":"2024-02-01","totalHours":9.0},{"day":"2024-02-02","totalHours":8.92}]}
{"name":"Sample Project B","hoursSpent":[{"day":"2024-02-01","totalHours":9.5}]}
```

### Benefits of NDJSON Streaming:
- **Memory Efficient**: Records are processed and sent one at a time
- **Real-time**: Client receives data as it's processed
- **Scalable**: Works with millions of records without memory issues
- **Standard Format**: Widely supported by streaming JSON parsers

## Database Schema

The application uses Flyway for database migrations:

### Tables
- **employee**: Employee information (id, name)
- **project**: Project information (id, name)  
- **time_record**: Time tracking records (id, employee_id, project_id, time_from, time_to)

### Sample Data
- Employees: Tom (101), Jerry (102)
- Projects: Sample Project A (1), Sample Project B (2)
- Time records for February 2024

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/example/reportingservice/
│   │       ├── controller/        # REST controllers
│   │       ├── model/             # Domain models
│   │       ├── repository/        # Reactive repositories
│   │       └── service/           # Business logic
│   └── resources/
│       ├── db/migration/          # Flyway database migrations
│       ├── application.yml        # Default configuration
│       └── application-local.yml  # Local development configuration
└── test/                          # Test files
```

## Development Features

- **Hot Reload**: Automatic application restart on code changes (via DevTools)
- **LiveReload**: Browser auto-refresh on changes
- **Database Migrations**: Flyway handles schema versioning
- **Reactive Stack**: WebFlux + R2DBC for non-blocking operations

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
