# Quarkus Rest Boilerplate

This project uses Quarkus, the Supersonic Subatomic Java Framework.

## Architecture
- **Framework**: Quarkus (reactive JAX-RS, not Spring)
- **Database**: PostgreSQL with Hibernate Reactive + Panache
- **Storage**: MinIO object storage, Redis caching
- **Main packages**: `core/` (entities, DTOs, use cases), `domain/` (services, repos), `infrastructure/` (config, errors, utils), `web/rest/` (controllers)
- **REST endpoints**: `/api/entity-devices`, `/demo`, `/common`, `/stream`

## Code Style
- **Formatting**: Palantir Java Format via Spotless plugin
- **Imports**: Order: `blank,java|javax,#` with unused imports removed
- **Annotations**: Lombok (`@Getter`, `@Slf4j`), Jakarta (`@Path`, `@GET`, `@Inject`), OpenAPI (`@Operation`, `@Tag`)
- **Reactive**: Use `Uni<T>` return types for async operations
- **DTOs**: Separate create/update DTOs, view models with `VM` suffix
- **Entities**: Extend `AbstractAuditingEntity`, use UUIDs for IDs

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .
## Getting Started
## Required Environment

- **Java**: Ensure you have JDK 21 or later installed.
- **Gradle**: Gradle 8.5 or later is required.
- **GraalVM**: Required for building native executables (optional).

You can find environment variables of application in [.env-template](.env.template)

## Infrastructure

- **Database**: Specify the database used (e.g., PostgreSQL, MySQL) and any setup instructions.
- **MinIO**: Used for video streaming. Ensure MinIO is set up and accessible.
- **Redis**: Used for caching. Ensure Redis is set up and accessible.
- **OpenTelemetry**: For distributed tracing, ensure OpenTelemetry is configured.

## Dependencies

This project uses the following key dependencies:
- **Quarkus**: Core framework for building Java applications.
- **Hibernate Reactive**: For persistence.
- **Mapstruct**: For object mapping.
- **RESTEasy Reactive**: For building RESTful web services.
- **Swagger**: For API documentation.

## Compiler

The project is built using Gradle. Ensure Gradle is installed and configured correctly.
After editing the source code, run the following command to compile the project if errors occur:
```shell script
./gradlew spotlessApply
```

- Format check: `./mvnw spotless:check`, Format apply: `./mvnw spotless:apply`

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  
> - Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev-ui/
> - API Documents at: http://localhost:8080/q/swagger-ui/

## Packaging and running the application

The application can be packaged using:
```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./gradlew build -Dquarkus.package.native=true
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./gradlew build -Dquarkus.package.native=true -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/quarkus-rest-1.0.0-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Provided Code

### RESTEasy Reactive

Easily start your Reactive RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)


## Service include:

- RestAPI with Reactive
- Middleware with Declaring Routes
- LogFilters
- Persistence with Hibernate Reactive
- Define Audit Data
- Mapper with Mapstruct
- Errors Handler
- Swagger Docs
