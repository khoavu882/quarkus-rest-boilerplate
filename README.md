# Demo Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .
## Getting Started
## Required Environment

- **Java**: Ensure you have JDK 11 or later installed.
- **Maven**: Apache Maven 3.6.3 or later is required.
- **GraalVM**: Required for building native executables (optional).

## Infrastructure

- **Database**: Specify the database used (e.g., PostgreSQL, MySQL) and any setup instructions.
- **MinIO**: Used for video streaming. Ensure MinIO is set up and accessible.

## Dependencies

This project uses the following key dependencies:
- **Quarkus**: Core framework for building Java applications.
- **Hibernate ORM Panache**: For persistence.
- **Mapstruct**: For object mapping.
- **RESTEasy Reactive**: For building RESTful web services.
- **Swagger**: For API documentation.

## Compiler

The project is built using Maven. Ensure Maven is installed and configured correctly.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/quarkus-rest-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Provided Code

### RESTEasy Reactive

Easily start your Reactive RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)


## Service include:

- RestAPI with Reactive
- Middleware with Declaring Routes
- LogFilters
- Persistence with Hibernate ORM
- Define Audit Data
- Mapper with Mapstruct
- Errors Handler
- Swagger Docs
