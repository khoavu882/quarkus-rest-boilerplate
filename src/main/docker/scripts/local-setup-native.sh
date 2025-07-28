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
NATIVE="⚡"

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
    local max_attempts=90  # Increased for native application startup
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

        # Check if it's GraalVM for native compilation
        if java -version 2>&1 | grep -q "GraalVM"; then
            print_success "GraalVM detected - native compilation supported"
        else
            print_warning "GraalVM not detected - native compilation may not work optimally"
            print_info "For best native compilation, use: sdk use java 21.0.7-graal"
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

# Function to check system resources for native build
check_system_resources() {
    print_step "Checking system resources for native build"

    # Check available memory (native builds are memory intensive)
    if command -v free &> /dev/null; then
        AVAILABLE_RAM=$(free -m | awk 'NR==2{printf "%.1f", $7/1024}')
        print_info "Available RAM: ${AVAILABLE_RAM}GB"

        if (( $(echo "$AVAILABLE_RAM < 4.0" | bc -l) )); then
            print_warning "Low available RAM detected. Native builds require ~4GB+ RAM"
            print_info "Consider closing other applications or increasing swap space"
        else
            print_success "Sufficient RAM available for native build"
        fi
    fi

    # Check available disk space (fix for macOS)
    if [[ "$OSTYPE" == "darwin"* ]]; then
        AVAILABLE_DISK=$(df -g . | awk 'NR==2 {print $4}')
    else
        AVAILABLE_DISK=$(df -BG . | awk 'NR==2 {print $4}' | sed 's/G//')
    fi

    if [[ -n "$AVAILABLE_DISK" && "$AVAILABLE_DISK" =~ ^[0-9]+$ ]]; then
        print_info "Available disk space: ${AVAILABLE_DISK}GB"
        if [ "$AVAILABLE_DISK" -lt 5 ]; then
            print_warning "Low disk space detected. Native builds require ~5GB+ free space"
        else
            print_success "Sufficient disk space available for native build"
        fi
    else
        print_info "Available disk space: Unable to determine"
        print_warning "Please ensure you have at least 5GB free disk space"
    fi
}

# Function to build the native application
build_native_application() {
    print_step "Building Quarkus native application (this may take 5-15 minutes)"
    print_warning "⚠️  Native builds are resource-intensive and may take significant time"
    print_info "💡 You can monitor progress in another terminal with: docker stats"
    echo ""

    # Clean and build native image using container-based compilation
    # Use the correct Quarkus native configuration properties
    print_info "Building native executable with container-based GraalVM compilation..."
    if ./gradlew clean build \
        -Dquarkus.package.jar.enabled=false \
        -Dquarkus.native.enabled=true \
        -Dquarkus.native.container-build=true \
        -Dquarkus.native.builder-image=quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-21 \
        -Dquarkus.container-image.build=false \
        -DENABLE_CONTAINER_IMAGE_BUILD=false \
        -DENABLE_NATIVE_CONTAINER_BUILD=true \
        -Dquarkus.native.native-image-xmx=4g \
        -x test; then
        print_success "Native application built successfully!"

        # Check for native executable in multiple possible locations
        NATIVE_EXEC=""

        # Look for the native executable (binary file, not JAR) - use pattern matching
        if ls build/*-runner 2>/dev/null | head -1 | grep -v "\.jar$" >/dev/null 2>&1; then
            NATIVE_EXEC=$(ls build/*-runner 2>/dev/null | grep -v "\.jar$" | head -1)
        fi

        if [ -n "$NATIVE_EXEC" ] && [ -f "$NATIVE_EXEC" ]; then
            print_info "Native executable found at: $NATIVE_EXEC"
            # Make sure it's executable
            chmod +x "$NATIVE_EXEC"

            # Test the native executable by checking its file type
            if file "$NATIVE_EXEC" | grep -q "executable\|ELF\|Mach-O"; then
                print_success "Native executable is valid and ready"

                # Get file size for comparison
                local file_size=$(du -h "$NATIVE_EXEC" | cut -f1)
                print_info "Native executable size: $file_size"

                # Quick test to ensure the executable works (with timeout)
                print_info "Testing native executable..."
                if timeout 5 "$NATIVE_EXEC" --help &>/dev/null || [ $? -eq 124 ]; then
                    print_success "Native executable test passed"
                else
                    print_warning "Native executable may have issues, but continuing..."
                fi
            else
                print_warning "Native executable found but may not be properly built"
                print_info "File type: $(file "$NATIVE_EXEC")"
            fi
        else
            print_error "Native executable not found after build"
            print_info "Checking what was actually built:"

            # Show what was created instead
            find build/ -name "*runner*" -type f 2>/dev/null | while read file; do
                if [[ "$file" == *.jar ]]; then
                    print_warning "Found JAR file (not native): $file"
                else
                    print_info "Found file: $file"
                    file "$file" 2>/dev/null | sed "s/^/    /" || echo "    Cannot determine file type"
                fi
            done

            print_error "Native compilation failed to create executable binary"
            print_info "💡 The build may have created JAR files instead of native executable"
            print_info "💡 Try the regular JVM build instead: ./src/main/docker/scripts/local-setup.sh"
            exit 1
        fi
    else
        print_error "Failed to build native application"
        print_info "💡 Common solutions:"
        print_info "   • Ensure you have sufficient RAM (4GB+ available)"
        print_info "   • Ensure you have sufficient disk space (5GB+ free)"
        print_info "   • Try restarting Docker if it's been running for a long time"
        print_info "   • Try the regular JVM build: ./src/main/docker/scripts/local-setup.sh"
        print_info "   • Check Docker logs: docker logs \$(docker ps -q --filter ancestor=quay.io/quarkus/ubi9-quarkus-mandrel-builder-image)"
        exit 1
    fi
}

# Function to create native Dockerfile if it doesn't exist
create_native_dockerfile() {
    NATIVE_DOCKERFILE="src/main/docker/Dockerfile.native"

    if [ ! -f "$NATIVE_DOCKERFILE" ]; then
        print_step "Creating native Dockerfile"

        cat > "$NATIVE_DOCKERFILE" << 'EOF'
####
# This Dockerfile is used in order to build a container that runs the Quarkus application in native (no JVM) mode.
# It uses a micro base image, providing a distroless container.
#
# Before building the container image run:
#
# ./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
#
# Then, build the image with:
#
# docker build -f src/main/docker/Dockerfile.native -t quarkus-rest-native .
#
# Then run the container using:
#
# docker run -i --rm -p 8080:8080 quarkus-rest-native
#
###
FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work

# Copy the native executable from the build directory
# The build context is the project root, so we reference build/ directly
COPY --chown=1001:root build/*-runner /work/application

EXPOSE 8080
USER 1001

ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
EOF
        print_success "Native Dockerfile created"
    else
        print_info "Native Dockerfile already exists"
    fi
}

# Function to create native docker-compose configuration
create_native_docker_compose() {
    NATIVE_COMPOSE="src/main/docker/docker-compose-native.yml"

    print_step "Creating native Docker Compose configuration"

    cat > "$NATIVE_COMPOSE" << 'EOF'
version: "3.8"
services:
  # Quarkus Native Application
  quarkus-app:
    build:
      context: ../../../
      dockerfile: src/main/docker/Dockerfile.native
    image: quarkus-rest-native:latest
    container_name: local_quarkus_native_app
    ports:
      - "8081:8080"   # Changed from 8080 to avoid conflict with JVM version
      - "9001:9000"   # Changed from 9000 to avoid conflict with JVM version
    environment:
      DEMO_HOST: postgresql
      DEMO_PORT: 5432
      DEMO_DB: db_local
      DEMO_USERNAME: postgres
      DEMO_PASSWORD: postgres
      DEMO_SCHEMA: sch_local
      ENABLE_QUARKUS_MANAGEMENT: "true"
      # Redis configuration
      QUARKUS_REDIS_HOSTS: redis://redis:6379
      # MinIO configuration
      MINIO_ENDPOINT: http://minio:9090
      MINIO_ACCESS_KEY: minioadmin
      MINIO_SECRET_KEY: minioadmin
      # Tracing configuration
      QUARKUS_OTEL_EXPORTER_OTLP_TRACES_ENDPOINT: http://jaeger:4318/v1/traces
    depends_on:
      postgresql:
        condition: service_healthy
      redis:
        condition: service_healthy
      minio:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8080/q/health || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 3
      start_period: 10s  # Native apps start much faster
    networks:
      - local_network
    # Native apps use much less memory
    deploy:
      resources:
        limits:
          memory: 256M
        reservations:
          memory: 128M

  # PostgreSQL Database
  postgresql:
    image: postgres:15-alpine
    container_name: local_postgres_native
    environment:
      POSTGRES_DB: db_local
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_INITDB_ARGS: "--encoding=UTF-8"
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 30s
      timeout: 10s
      retries: 5
    networks:
      - local_network
    volumes:
      - pgdata_native:/var/lib/postgresql/data

  # Redis Cache
  redis:
    image: redis:7-alpine
    container_name: local_redis_native
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 30s
      timeout: 10s
      retries: 5
    command: redis-server --appendonly yes --maxmemory 256mb --maxmemory-policy allkeys-lru
    networks:
      - local_network

  # MinIO Object Storage
  minio:
    image: minio/minio:latest
    container_name: local_minio_native
    ports:
      - "9090:9000"   # API
      - "9091:9001"   # Console
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    command: server /data --console-address ":9001"
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:9000/minio/health/live || exit 1"]
      interval: 30s
      timeout: 20s
      retries: 3
    networks:
      - local_network
    volumes:
      - minio_data_native:/data

  # Jaeger
  jaeger:
    image: jaegertracing/all-in-one:latest
    container_name: local_jaeger_native
    ports:
      - "16686:16686" # Jaeger UI
      - "4317:4317"   # OTLP gRPC receiver
      - "4318:4318"   # OTLP HTTP receiver
    environment:
      - COLLECTOR_OTLP_ENABLED=true
    networks:
      - local_network

networks:
  local_network:
    driver: bridge
    name: local_network_native

volumes:
  pgdata_native:
  minio_data_native:
EOF
    print_success "Native Docker Compose configuration created"
}

# Function to start Docker services with native image
start_docker_services() {
    print_step "Starting Docker Compose services with native application"

    # Remove orphaned containers if any
    docker compose -f src/main/docker/docker-compose-native.yml down --remove-orphans &> /dev/null

    # Build and start services
    if docker compose -f src/main/docker/docker-compose-native.yml up -d --build; then
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

    print_info "${NATIVE} Quarkus Native Application: localhost:8081"
    check_service "Quarkus Native Application" 8081

    echo ""
    print_success "All services are ready!"
}

# Function to show service URLs
show_service_urls() {
    print_header "Service Access URLs"
    echo -e "${CYAN}📊 Application URLs (Native):${NC}"
    echo -e "   ${WHITE}• Quarkus App:${NC}        http://localhost:8081 ${NATIVE}"
    echo -e "   ${WHITE}• Swagger UI:${NC}         http://localhost:8081/q/swagger-ui"
    echo -e "   ${WHITE}• Health Check:${NC}       http://localhost:8081/q/health"
    echo -e "   ${WHITE}• Metrics:${NC}            http://localhost:8081/q/metrics"
    echo -e "   ${WHITE}• Management:${NC}         http://localhost:9001/q/health"
    echo ""
    echo -e "${CYAN}🔧 Infrastructure URLs:${NC}"
    echo -e "   ${WHITE}• MinIO API:${NC}          http://localhost:9090"
    echo -e "   ${WHITE}• MinIO Console:${NC}      http://localhost:9091 (minioadmin/minioadmin)"
    echo -e "   ${WHITE}• Jaeger UI:${NC}          http://localhost:16686"
    echo -e "   ${WHITE}• PostgreSQL:${NC}         localhost:5432 (postgres/postgres)"
    echo -e "   ${WHITE}• Redis:${NC}              localhost:6379"
    echo ""
    echo -e "${GREEN}${CHECK_MARK} All services are running with ${NATIVE} NATIVE APPLICATION!${NC}"
    echo -e "${PURPLE}🚀 Native Benefits:${NC}"
    echo -e "   ${WHITE}• Faster startup time (milliseconds vs seconds)${NC}"
    echo -e "   ${WHITE}• Lower memory usage (~50-80% reduction)${NC}"
    echo -e "   ${WHITE}• Instant peak performance${NC}"
    echo -e "   ${WHITE}• Smaller container images${NC}"
    echo ""
    echo -e "${BLUE}ℹ️  Use 'docker compose -f src/main/docker/docker-compose-native.yml logs -f' to view logs${NC}"
    echo -e "${BLUE}ℹ️  Use 'docker compose -f src/main/docker/docker-compose-native.yml down' to stop all services${NC}"
    echo ""
}

# Function to show performance comparison
show_performance_info() {
    print_header "Native vs JVM Performance"
    echo -e "${CYAN}${NATIVE} Native Application Benefits:${NC}"
    echo -e "   ${GREEN}• Startup Time:${NC}     ~50-100ms (vs 2-5s JVM)"
    echo -e "   ${GREEN}• Memory Usage:${NC}     ~50-100MB (vs 200-500MB JVM)"
    echo -e "   ${GREEN}• Image Size:${NC}       ~50-100MB (vs 200-400MB JVM)"
    echo -e "   ${GREEN}• CPU Usage:${NC}        Lower baseline consumption"
    echo -e "   ${GREEN}• Cold Starts:${NC}      Excellent for serverless/containers"
    echo ""
    echo -e "${YELLOW}⚠️  Native Application Trade-offs:${NC}"
    echo -e "   ${WHITE}• Build Time:${NC}       5-15 minutes (vs 30-60s JVM)"
    echo -e "   ${WHITE}• Build Resources:${NC}  High CPU/Memory requirements"
    echo -e "   ${WHITE}• Reflection:${NC}       Limited runtime reflection"
    echo -e "   ${WHITE}• Dynamic Loading:${NC}  Restricted class loading"
    echo ""
}

# Function to show application logs
show_application_logs() {
    print_header "Native Application Logs"
    print_info "Following Quarkus native application logs (Ctrl+C to exit)"
    echo ""

    # Follow the application logs
    docker compose -f src/main/docker/docker-compose-native.yml logs -f quarkus-app
}

# Main execution flow
main() {
    print_header "${ROCKET} Quarkus REST Native Application Docker Setup"
    show_performance_info

    # Pre-flight checks
    check_java_version
    check_docker
    check_system_resources

    # Prepare native build environment
    print_header "${BUILD} Preparing Native Build Environment"
    create_native_dockerfile
    create_native_docker_compose

    # Build native application
    print_header "${NATIVE} Building Native Application"
    build_native_application

    # Start infrastructure and native application
    print_header "Starting All Services with Native Application"
    start_docker_services
    wait_for_services

    # Show service information
    show_service_urls

    # Show application logs
    show_application_logs
}

# Trap to handle script interruption
trap 'echo -e "\n${YELLOW}⚠️  Setup interrupted. Stopping services...${NC}"; docker compose -f src/main/docker/docker-compose-native.yml down; exit 1' INT TERM

# Run main function
main
