# Quarkus REST Boilerplate

A reactive REST API boilerplate built on Quarkus 3.23.4 (Java 21), demonstrating hexagonal/clean
architecture end to end with Mutiny reactive types. `EntityDevice` is the worked example — new
features should follow its shape (see [ARCHITECTURE.md](docs/ARCHITECTURE.md)).

## Quick start

```bash
sdk env
./src/main/docker/scripts/local-setup.sh
```

This builds the app and starts every dependency (PostgreSQL, Redis, MinIO, Jaeger) in Docker, then
prints all service URLs once ready — including Swagger UI at http://localhost:8080/q/swagger-ui.

For native-image builds, running infra separately from a hot-reload dev loop, environment variables,
and troubleshooting, see **[docs/SETUP.md](docs/SETUP.md)**.

## Tech stack

- **Java 21** + **Quarkus 3.23.4** (reactive JAX-RS, Mutiny)
- **PostgreSQL** via Hibernate Reactive Panache (no blocking JDBC)
- **Redis** (cache-aside via `CacheService`) + **Caffeine** for message-bundle caching
- **MinIO** for object storage, with HTTP range-request media streaming
- **MapStruct** for entity/DTO mapping, **Jakarta Bean Validation** with custom validators
- **Micrometer/Prometheus** metrics, **OpenTelemetry/Jaeger** tracing, **SmallRye Health**
- **Spotless** (Palantir Java Format) for formatting

## Key endpoints

- `/api/entity-devices` — CRUD, filtering, and pagination for the `EntityDevice` reference example
- `/stream` — range-request media streaming from MinIO
- `/common` — utility endpoints (i18n bundle lookup, etc.)
- `/observability` — health/metrics-adjacent endpoints
- `/q/swagger-ui`, `/q/health`, `/q/metrics` — Quarkus-provided operational endpoints

## Documentation

- **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** — layering, error-handling design, caching,
  observability, configuration, and known limitations
- **[docs/SETUP.md](docs/SETUP.md)** — local dev options, build/test commands, environment
  variables, service URLs, troubleshooting
- **[CHANGELOG.md](CHANGELOG.md)** — release history

## Known limitations

No automated test suite exists yet, and `@RolesAllowed`/the OpenAPI JWT security scheme are
declarative only (no identity provider is wired in) — see
[ARCHITECTURE.md#known-limitations](docs/ARCHITECTURE.md#known-limitations) before treating this as
production-ready.
