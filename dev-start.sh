#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting development environment...${NC}"

# Function to cleanup on exit
cleanup() {
    echo -e "\n${YELLOW}Stopping application...${NC}"
    if [ ! -z "$APP_PID" ]; then
        kill $APP_PID 2>/dev/null
        wait $APP_PID 2>/dev/null
    fi
    echo -e "${GREEN}Application stopped. Docker containers remain running.${NC}"
    echo -e "${YELLOW}To stop PostgreSQL: docker-compose -f docker-compose.dev.yml down${NC}"
    exit 0
}

# Trap Ctrl-C to only stop the app, not docker-compose
trap cleanup SIGINT SIGTERM

# Start or restart docker-compose
echo -e "${GREEN}Starting PostgreSQL container...${NC}"
docker-compose -f docker-compose.dev.yml up -d

# Wait for PostgreSQL to be ready
echo -e "${YELLOW}Waiting for PostgreSQL to be ready...${NC}"
until docker exec reporting-postgres-local pg_isready -U postgres > /dev/null 2>&1; do
    sleep 1
done
echo -e "${GREEN}PostgreSQL is ready!${NC}"

# Start the Spring Boot application
echo -e "${GREEN}Starting Spring Boot application with hot reload...${NC}"
./mvnw spring-boot:run -Dspring-boot.run.profiles=local &
APP_PID=$!

echo -e "${GREEN}Development environment is running!${NC}"
echo -e "${YELLOW}Application: http://localhost:8080${NC}"
echo -e "${YELLOW}Press Ctrl-C to stop the application (PostgreSQL will keep running)${NC}"

# Wait for the application process
wait $APP_PID
