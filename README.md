# Quarkus REST Application

A modern reactive REST API built with Quarkus framework following hexagonal/clean architecture principles.

## Quick Start

### 🚀 **One-Command Docker Setup**
```bash
# Switch to Java 21 and run the enhanced Docker setup script
sdk use java 21.0.7-graal
./src/main/docker/scripts/local-setup.sh
```

### ⚡ **Native Docker Setup** (Advanced)
```bash
# For native image with faster startup and lower memory usage (5-15 min build time)
sdk use java 21.0.7-graal
./src/main/docker/scripts/local-setup-native.sh
```

**Note**: The native setup uses different ports to avoid conflicts with the JVM version:
- **Native App**: http://localhost:8081 (vs 8080 for JVM)
- **Native Management**: http://localhost:9001/q/health (vs 9000 for JVM)

**Important**: Ensure the `.dockerignore` file includes `!build/*-runner` to allow Docker access to the native executable during the build process.

The enhanced Docker setup script provides:
- ✅ **Pre-flight checks** for Java version and Docker availability
- 🎨 **Colored output** with progress indicators and status updates
- 🔍 **Service health monitoring** with automatic port connectivity checks
- 📊 **Service URLs display** showing all access points when ready
- 🛡️ **Graceful error handling** with clear troubleshooting information
- 🐳 **Complete containerization** - runs everything in Docker containers

## Tech Stack & Features

### Core Technologies
- **Java 21** with **Quarkus 3.23.4** (reactive JAX-RS)
- **Database**: PostgreSQL with Hibernate Reactive + Panache
- **Caching**: Redis client for distributed caching
- **Storage**: MinIO object storage integration
- **Reactive**: Mutiny for non-blocking operations
- **Validation**: Jakarta Bean Validation with custom validators
- **Mapping**: MapStruct for entity-DTO conversions
- **Scheduling**: Quartz scheduler integration

### Monitoring & Observability
- **Metrics**: Micrometer with Prometheus registry
- **Tracing**: OpenTelemetry distributed tracing with Jaeger
- **Health**: SmallRye Health checks with automatic service monitoring
- **Logging**: JSON structured logging with trace context
- **API Documentation**: OpenAPI/Swagger UI

### Additional Features
- **WebSockets**: Real-time communication support
- **Templating**: Qute template engine
- **File Processing**: Apache Tika for media file handling
- **Code Quality**: Spotless with Palantir Java Format
- **Docker**: Complete containerization with optimized multi-layer builds

## Architecture

### Hexagonal Architecture Structure
```
src/main/java/com/github/kaivu/
├── Application.java                 # Main application entry point
├── adapter/                         # External interfaces
│   ├── in/                         # Inbound adapters
│   │   ├── rest/                   # REST controllers
│   │   │   ├── CommonResource.java
│   │   │   ├── DemoResource.java
│   │   │   ├── EntityDevicesResource.java
│   │   │   ├── StreamingResource.java
│   │   │   ├── dto/                # Request/Response DTOs
│   │   │   └── validator/          # Custom validators
│   │   └── filter/                 # Request filters
│   └── out/                        # Outbound adapters
│       ├── api/                    # External API clients
│       ├── client/                 # HTTP clients
│       ├── persistence/            # Database repositories
│       ├── handler/                # Event handlers
│       └── exception/              # Adapter exceptions
├── application/                     # Application layer
│   ├── service/                    # Application services
│   ├── usecase/                    # Use case implementations
│   ├── port/                       # Port interfaces
│   └── exception/                  # Application exceptions
├── domain/                         # Domain layer
│   ├── EntityDevice.java           # Core entities
│   ├── MediaFile.java
│   ├── AbstractAuditingEntity.java # Base audit entity
│   ├── enumeration/                # Domain enums
│   ├── supplier/                   # Value suppliers
│   ├── audit/                      # Audit components
│   └── type/                       # Custom types
├── common/                         # Shared utilities
└── config/                         # Configuration classes
```

### Key REST Endpoints
- **`/api/entity-devices`** - CRUD operations for entity device management
  - GET, POST, PUT, DELETE operations
  - Filtering and pagination support
  - File upload capabilities
- **`/demo`** - Demo endpoints for testing
- **`/common`** - Common utility endpoints
- **`/stream`** - WebSocket streaming endpoints

## Docker Infrastructure

### Services Overview
| Service | Ports | Purpose | Access |
|---------|-------|---------|---------|
| **Quarkus App** | 8080, 9000 | Main application + Management | `http://localhost:8080` |
| **PostgreSQL** | 5432 | Primary database | `postgres:postgres@localhost:5432/db_local` |
| **Redis** | 6379 | Distributed caching | `localhost:6379` |
| **MinIO** | 9090, 9091 | Object storage | API: `localhost:9090`, Console: `http://localhost:9091` |
| **Jaeger** | 16686, 4317, 4318 | Distributed tracing | UI: `http://localhost:16686` |

### Docker Compose Features
- **Complete containerization** - Application runs as a Docker container
- **Health checks** for all services with automatic retry logic
- **Service dependencies** - Application waits for infrastructure to be ready
- **Named networks** (`local_network`) for secure service communication
- **Persistent volumes** for PostgreSQL data and MinIO storage
- **Environment-specific** configuration with proper resource limits

### Manual Docker Operations
```bash
# Start all services (including the Quarkus app)
docker compose -f src/main/docker/docker-compose.yml up -d --build

# View service logs
docker compose -f src/main/docker/docker-compose.yml logs -f [service_name]

# View application logs specifically
docker compose -f src/main/docker/docker-compose.yml logs -f quarkus-app

# Stop all services
docker compose -f src/main/docker/docker-compose.yml down

# Reset all data (removes volumes)
docker compose -f src/main/docker/docker-compose.yml down -v

# Rebuild application image
docker compose -f src/main/docker/docker-compose.yml build quarkus-app
```

## Development Workflow

### Enhanced Docker Setup Script

The `src/main/docker/scripts/local-setup.sh` script provides a comprehensive containerized development environment:

#### Features
- 🎨 **Visual feedback** with colored output and Unicode symbols
- 🔍 **Smart service monitoring** - waits for each service to be ready
- 📋 **Pre-flight validation** - checks Java version and Docker status
- 📊 **Service information display** - shows all URLs and credentials
- 🛡️ **Error handling** - graceful interruption and cleanup
- 🐳 **Docker integration** - builds and runs the application in containers

#### Usage
```bash
# Make script executable (first time only)
chmod +x src/main/docker/scripts/local-setup.sh

# Run the enhanced Docker setup
./src/main/docker/scripts/local-setup.sh
```

#### What the Script Does
1. **Environment Validation**: Checks Java 21+ and Docker availability
2. **Application Build**: Compiles the Quarkus application with Gradle
3. **Docker Image Creation**: Builds the application Docker image
4. **Infrastructure Startup**: Launches PostgreSQL, Redis, MinIO, and Jaeger
5. **Application Deployment**: Starts the Quarkus app container
6. **Health Monitoring**: Waits for all services to be ready (max 60s each)
7. **Information Display**: Shows all service URLs and access credentials
8. **Log Streaming**: Follows application logs in real-time

### Build & Run Commands
```bash
# Quick start with complete Docker setup
./src/main/docker/scripts/local-setup.sh

# Development mode (local, requires running infrastructure)
./gradlew quarkusDev

# Build application
./gradlew build

# Build with container image
./gradlew build -Dquarkus.container-image.build=true

# Build without container image
./gradlew build -Dquarkus.container-image.build=false

# Build native image (container-based)
./gradlew build -Dquarkus.native.container-build=true

# Build native image (local, requires GraalVM)
./gradlew build -Dquarkus.package.native=true

# Run tests
./gradlew test

# Format code (required before commit)
./gradlew spotlessApply

# Check code formatting
./gradlew spotlessCheck

# Clean build
./gradlew clean build
```

### Development URLs

Once the Docker setup completes, access these URLs:

#### 📊 Application Endpoints
- **Quarkus App**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **Health Checks**: http://localhost:8080/q/health
- **Metrics**: http://localhost:8080/q/metrics
- **Management**: http://localhost:9000/q/health

#### 🔧 Infrastructure Services
- **MinIO API**: http://localhost:9090
- **MinIO Console**: http://localhost:9091 (minioadmin/minioadmin)
- **Jaeger Tracing**: http://localhost:16686
- **PostgreSQL**: `localhost:5432` (postgres/postgres)
- **Redis**: `localhost:6379`

## Testing Strategy

The application uses JUnit 5 with Quarkus testing extensions:
- **Integration Tests**: REST endpoint testing with RESTAssured
- **Mock Support**: Panache Mock for repository testing
- **Health Checks**: Automated health endpoint validation
- **Container Support**: Docker image building and testing
- **Database Testing**: TestContainers for integration testing

## Configuration

### Environment Variables
- **Database**: `DEMO_DB_KIND`, `DEMO_HOST`, `DEMO_PORT`, `DEMO_DB`, `DEMO_SCHEMA`
- **Credentials**: `DEMO_USERNAME`, `DEMO_PASSWORD`
- **Application**: `APP_NAME`, `ENABLE_QUARKUS_MANAGEMENT`
- **Storage**: `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`
- **Caching**: `QUARKUS_REDIS_HOSTS`

### Database Schema
- **Default Schema**: `sch_local` (configurable via `DEMO_SCHEMA`)
- **Generation**: `create-drop` for development
- **Connection Pool**: 16 max connections per datasource

### Observability
- **Management Port**: 9000 (when enabled)
- **Trace Context**: Automatic traceId, spanId injection in logs
- **Health Checks**: Available at `/q/health`
- **Metrics**: Prometheus format at `/q/metrics`
- **OpenAPI**: Documentation at `/q/swagger-ui`

## Getting Started

### Prerequisites
- **Java 21+** (recommended: GraalVM 21.0.7 via SDKMAN)
- **Docker & Docker Compose** for containerized deployment
- **Git** for version control

### Development Setup
1. **Clone & Navigate**
   ```bash
   git clone <repository-url>
   cd quarkus-rest
   ```

2. **Java Version** (if using SDKMAN)
   ```bash
   sdk use java 21.0.7-graal
   ```

3. **One-Command Docker Setup**
   ```bash
   ./src/main/docker/scripts/local-setup.sh
   ```

4. **Verify Setup** - The script will display all service URLs when ready

5. **Access Application** - Visit http://localhost:8080 to access the running application

### Troubleshooting

#### Port Conflicts
The setup resolves common port conflicts automatically. If you encounter issues, check:
```bash
# Check specific ports
lsof -i :8080  # Quarkus App
lsof -i :5432  # PostgreSQL
lsof -i :6379  # Redis
lsof -i :9090  # MinIO API
lsof -i :9001  # MinIO Console
lsof -i :9000  # Quarkus Management
```

#### Java Version Issues
Ensure you're using Java 21+:
```bash
java -version
# If using SDKMAN:
sdk use java 21.0.7-graal
./gradlew --stop  # Restart Gradle daemon with new Java version
```

#### Docker Issues
Verify Docker is running and has sufficient resources:
```bash
docker ps
docker compose version
docker system df  # Check disk usage
docker system prune  # Clean up if needed
```

#### Application Logs
Monitor application logs for debugging:
```bash
# Follow all service logs
docker compose -f src/main/docker/docker-compose.yml logs -f

# Follow only application logs
docker compose -f src/main/docker/docker-compose.yml logs -f quarkus-app

# Check service health
docker compose -f src/main/docker/docker-compose.yml ps
```

### Development Tips

- **Hot Reload**: For development mode with hot reload, use `./gradlew quarkusDev` after starting infrastructure
- **Database Access**: Connect to PostgreSQL at `localhost:5432` with credentials `postgres/postgres`
- **Object Storage**: Access MinIO console at http://localhost:9001 with `minioadmin/minioadmin`
- **Distributed Tracing**: View request traces in Jaeger UI at http://localhost:16686
- **API Documentation**: Explore the API at http://localhost:8080/q/swagger-ui
