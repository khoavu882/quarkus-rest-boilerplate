#!/bin/bash

# Colors for better visual output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m' # No Color

# Unicode symbols for better visual feedback
CHECK_MARK="✅"
CROSS_MARK="❌"
PROGRESS="⏳"
ROCKET="🚀"
DATABASE="🗄️"
CACHE="💾"
STORAGE="📦"
TRACE="🔍"
COFFEE="☕"
BUILD="🔨"

# Function to print decorated messages
print_header() {
    echo ""
    echo -e "${PURPLE}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${WHITE}  $1${NC}"
    echo -e "${PURPLE}════════════════════════════════════════════════════════════════${NC}"
    echo ""
}

print_step() {
    echo -e "${CYAN}${PROGRESS} $1...${NC}"
}

print_success() {
    echo -e "${GREEN}${CHECK_MARK} $1${NC}"
}

print_error() {
    echo -e "${RED}${CROSS_MARK} $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Function to check if a service is running
check_service() {
    local service_name=$1
    local port=$2
    local max_attempts=60  # Increased for application startup
    local attempt=1

    print_step "Waiting for $service_name on port $port"

    while [ $attempt -le $max_attempts ]; do
        if nc -z localhost $port 2>/dev/null; then
            print_success "$service_name is ready on port $port"
            return 0
        fi

        printf "${YELLOW}.${NC}"
        sleep 1
        ((attempt++))
    done

    echo ""
    print_error "$service_name failed to start on port $port after $max_attempts seconds"
    return 1
}

# Function to check Java version
check_java_version() {
    print_step "Checking Java version"

    if command -v java &> /dev/null; then
        # Get full Java version string
        JAVA_VERSION_FULL=$(java -version 2>&1 | head -n 1)
        # Extract major version number (works for both old and new versioning schemes)
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | sed 's/.*version "\([0-9]*\)\..*/\1/' | sed 's/.*version "\([0-9]*\)".*/\1/')

        if [ "$JAVA_VERSION" -ge "17" ]; then
            print_success "Java $JAVA_VERSION detected"
            echo "    $JAVA_VERSION_FULL" | sed "s/^/    /"
        else
            print_warning "Java $JAVA_VERSION detected, but Java 17+ is recommended"
            print_info "Consider running: sdk use java 21.0.7-graal"
        fi
    else
        print_error "Java not found in PATH"
        exit 1
    fi
}

# Function to check Docker
check_docker() {
    print_step "Checking Docker availability"

    if command -v docker &> /dev/null; then
        if docker ps &> /dev/null; then
            print_success "Docker is running"
            docker --version | sed "s/^/    /"
        else
            print_error "Docker is installed but not running"
            exit 1
        fi
    else
        print_error "Docker not found in PATH"
        exit 1
    fi
}

# Function to build the application
build_application() {
    print_step "Building Quarkus application"

    if ./gradlew build -x test; then
        print_success "Application built successfully"
    else
        print_error "Failed to build application"
        exit 1
    fi
}

# Function to start Docker services
start_docker_services() {
    print_step "Starting Docker Compose services"

    # Remove orphaned containers if any
    docker compose -f src/main/docker/docker-compose.yml down --remove-orphans &> /dev/null

    # Build and start services
    if docker compose -f src/main/docker/docker-compose.yml up -d --build; then
        print_success "Docker services started successfully"
    else
        print_error "Failed to start Docker services"
        exit 1
    fi
}

# Function to wait for all services
wait_for_services() {
    print_header "Waiting for Services to Initialize"

    print_info "${DATABASE} PostgreSQL: localhost:5432"
    check_service "PostgreSQL" 5432

    print_info "${CACHE} Redis: localhost:6379"
    check_service "Redis" 6379

    print_info "${STORAGE} MinIO: localhost:9090"
    check_service "MinIO" 9090

    print_info "${TRACE} Jaeger: localhost:16686"
    check_service "Jaeger" 16686

    print_info "${COFFEE} Quarkus Application: localhost:8080"
    check_service "Quarkus Application" 8080

    echo ""
    print_success "All services are ready!"
}

# Function to show service URLs
show_service_urls() {
    print_header "Service Access URLs"
    echo -e "${CYAN}📊 Application URLs:${NC}"
    echo -e "   ${WHITE}• Quarkus App:${NC}        http://localhost:8080"
    echo -e "   ${WHITE}• Swagger UI:${NC}         http://localhost:8080/q/swagger-ui"
    echo -e "   ${WHITE}• Health Check:${NC}       http://localhost:8080/q/health"
    echo -e "   ${WHITE}• Metrics:${NC}            http://localhost:8080/q/metrics"
    echo -e "   ${WHITE}• Management:${NC}         http://localhost:9000/q/health"
    echo ""
    echo -e "${CYAN}🔧 Infrastructure URLs:${NC}"
    echo -e "   ${WHITE}• MinIO Console:${NC}      http://localhost:9091 (minioadmin/minioadmin)"
    echo -e "   ${WHITE}• Jaeger UI:${NC}          http://localhost:16686"
    echo -e "   ${WHITE}• PostgreSQL:${NC}         localhost:5432 (postgres/postgres)"
    echo -e "   ${WHITE}• Redis:${NC}              localhost:6379"
    echo ""
    echo -e "${GREEN}${CHECK_MARK} All services are running in Docker containers!${NC}"
    echo -e "${BLUE}ℹ️  Use 'docker compose -f src/main/docker/docker-compose.yml logs -f' to view logs${NC}"
    echo -e "${BLUE}ℹ️  Use 'docker compose -f src/main/docker/docker-compose.yml down' to stop all services${NC}"
    echo ""
}

# Function to show application logs
show_application_logs() {
    print_header "Application Logs"
    print_info "Following Quarkus application logs (Ctrl+C to exit)"
    echo ""

    # Follow the application logs
    docker compose -f src/main/docker/docker-compose.yml logs -f quarkus-app
}

# Main execution flow
main() {
    print_header "${ROCKET} Quarkus REST Application Docker Setup"

    # Pre-flight checks
    check_java_version
    check_docker

    # Build application
    print_header "${BUILD} Building Application"
    build_application

    # Start infrastructure and application
    print_header "Starting All Services in Docker"
    start_docker_services
    wait_for_services

    # Show service information
    show_service_urls

    # Show application logs
    show_application_logs
}

# Trap to handle script interruption
trap 'echo -e "\n${YELLOW}⚠️  Setup interrupted. Stopping services...${NC}"; docker compose -f src/main/docker/docker-compose.yml down; exit 1' INT TERM

# Run main function
main
