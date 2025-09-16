#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Stopping development environment...${NC}"

# Stop and remove PostgreSQL container
echo -e "${GREEN}Stopping PostgreSQL container...${NC}"
docker-compose -f docker-compose.dev.yml down

echo -e "${GREEN}Development environment stopped completely.${NC}"
