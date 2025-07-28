# Quarkus REST Application

A modern reactive REST API built with Quarkus framework following hexagonal/clean architecture principles.

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
- **Tracing**: OpenTelemetry distributed tracing
- **Health**: SmallRye Health checks
- **Logging**: JSON structured logging
- **API Documentation**: OpenAPI/Swagger UI

### Additional Features
- **WebSockets**: Real-time communication support
- **Templating**: Qute template engine
- **File Processing**: Apache Tika for media file handling
- **Code Quality**: Spotless with Palantir Java Format

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

## Development Workflow

### Build & Run Commands
```bash
# Development mode with hot reload
./gradlew quarkusDev

# Build application
./gradlew build

# Run tests
./gradlew test

# Format code (required before commit)
./gradlew spotlessApply

# Check code formatting
./gradlew spotlessCheck

# Clean build
./gradlew clean build
```

### Code Style & Conventions

#### Formatting & Structure
- **Code Formatting**: Palantir Java Format enforced via Spotless plugin
- **Import Order**: blank line, java/javax imports, then static imports (#-prefixed)
- **Naming Conventions**: 
  - REST resources end with `Resource`
  - Services end with `Service`
  - Use cases end with `UseCase`
  - View models end with `VM` suffix

#### Reactive Programming
- Use `Uni<T>` return types for async operations
- Apply `@WithTransaction` for transactional methods
- Avoid blocking calls in reactive chains
- Leverage Mutiny operators for composition

#### Data & Validation
- **Entities**: Extend `AbstractAuditingEntity` for audit fields
- **IDs**: Use UUIDs for entity identifiers
- **DTOs**: Separate create/update DTOs with validation annotations
- **Custom Validation**: Use `@ValidEnumValue` and similar custom validators
- **Database**: Soft delete pattern with `@Filter` annotations

#### Error Handling
- Custom exceptions extend `ServiceException`
- Use `ErrorResponse` DTOs for consistent error responses
- Implement proper HTTP status codes
- Include trace IDs for debugging

## Configuration

### Environment Variables
- **Database**: `DEMO_DB_KIND`, `DEMO_HOST`, `DEMO_PORT`, `DEMO_DB`, `DEMO_SCHEMA`
- **Credentials**: `DEMO_USERNAME`, `DEMO_PASSWORD`
- **Application**: `APP_NAME`, `ENABLE_QUARKUS_MANAGEMENT`

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

1. **Prerequisites**: Java 21, Docker (for PostgreSQL/Redis)
2. **Database Setup**: Configure PostgreSQL connection in `application.yml`
3. **Development**: Run `./gradlew quarkusDev` for hot reload
4. **Dev UI**: Access Dev UI at `http://localhost:8080/q/dev-ui`
5. **API Testing**: Access Swagger UI at `http://localhost:8080/q/swagger-ui`
6. **Code Quality**: Always run `./gradlew spotlessApply` before committing

## Testing Strategy

The application uses JUnit 5 with Quarkus testing extensions:
- **Integration Tests**: REST endpoint testing with RESTAssured
- **Mock Support**: Panache Mock for repository testing
- **Health Checks**: Automated health endpoint validation
- **Container Support**: Docker image building and testing
