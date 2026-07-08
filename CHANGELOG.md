# Changelog

All notable changes to this project are documented in this file, derived from the git history and
grouped by release tag. Format loosely follows [Keep a Changelog](https://keepachangelog.com/).

## [Unreleased]

### Fixed
- Removed a shared-mutable-state race in the error-handling enums (`AppErrorEnum`, `ErrorsEnum`,
  `ClientErrorsEnum`): they used to mutate a `message` field on the shared enum singleton, which was
  unsafe under concurrent reactive requests. Message resolution is now stateless per call;
  `ServiceException` carries its own locale/args instead.
- `CompositeExceptionMapper`'s exception-type dispatch used `Map.of(...)`, whose iteration order is
  unspecified — since every specific exception type also matches the generic `ServiceException`
  fallback in the same map, dispatch could nondeterministically resolve to the wrong HTTP status.
  Rebuilt as an explicitly ordered dispatch table.
- `RedisHelperImpl.deleteByPattern`/`clear` used the blocking `KEYS` command (locks the whole
  keyspace in one pass); switched to the non-blocking `SCAN` cursor.
- `MinioHelperImpl` ran blocking MinIO SDK calls directly on the Vert.x event loop; offloaded to a
  virtual-thread executor.
- `PageableRequest` had `@Size` on primitive `int` fields — a silent no-op, since `@Size` doesn't
  support primitives — leaving page size effectively unbounded. Replaced with `@Min`/`@Max`.
- A missing i18n bundle key could crash the exception mapper trying to render the error; the message
  resolver now falls back to the raw key and logs instead of throwing.
- Two `log.error(...)` calls were passing `ex.getMessage()` instead of the throwable, discarding the
  stack trace on failure paths that need it most.
- A hand-rolled response filter (`HttpFilters.handleCompression`) gzip-compressed response bodies but
  its `Content-Encoding: gzip` header write never reached the actual HTTP response, so any client
  honoring HTTP semantics (browsers, Swagger UI, `curl`) received unparseable compressed bytes labeled
  as plain JSON. Removed — redundant with the already-present, correctly-implemented
  `quarkus.http.enable-compression`.
- `MissingResourceExceptionMapper` — the mapper whose job is handling a bundle-lookup failure —
  resolved its own response message via a raw, throwable `ResourceBundle` lookup instead of the safe,
  cached, fallback-protected path every other error in the codebase uses, meaning it could itself
  throw the same exception it exists to handle. The duplicated cache+fallback logic in `ErrorsEnum`
  and `ClientErrorsEnum` is now consolidated into `ResourceBundleUtil.getKeyWithResourceBundleOrFallback`.
- `EntityDeviceUseCaseImpl.delete()` invoked the page-cache invalidation `Uni` from inside `.invoke()`
  instead of `.call()`, so it was built but never subscribed to — Mutiny does nothing until
  something subscribes. Deleted entities kept appearing in cached list/page results until the cache
  TTL expired.

### Changed
- `AppMetrics`'s cache hit/miss/error counters were tracked only in-process; registered as Micrometer
  gauges so they reach the same Prometheus pipeline as the app's other metrics.
- Simplified `application.yml`: dropped redundant `${ENV_VAR:default}` wrappers on properties that
  only ever appear once (Quarkus's `EnvConfigSource` auto-maps env vars to any property path
  regardless of explicit expression syntax); kept the explicit form wherever one env var
  intentionally feeds multiple property paths (e.g. Redis default + demo client blocks).

## [v1.0.7] — 2025-08-06
### Added
- `docker-compose-dependencies.yml` for running infrastructure only, app via `quarkusDev`.

### Changed
- Metrics resource and error-enum-as-interface pattern (`AppErrorEnum`).
- Environment/config pattern cleanup; Java formatting pass.

## [v1.0.6] — 2025-07-25
### Added
- Docker Compose setup for local environment, with setup scripts and README instructions.
- Redis-backed and Caffeine message caching.

### Changed
- Migrated to a mixed clean/hexagonal architecture layering (`adapter` / `application` / `domain`).
- Reworked the MinIO integration to a provider + client pattern.

## [v1.0.5] — 2025-06-12
### Added
- `EntityDevice` CRUD API (create/read/update/delete, filtering, pagination) as the reference
  worked example for the boilerplate.

## [v1.0.4] — 2025-03-13
### Changed
- Bumped Quarkus 3.18.2 → 3.19.2.

## [v1.0.3] — 2025-02-11
### Added
- MinIO provider integration.
- Maven-based CI workflow (later replaced by Gradle).

### Changed
- Bumped Quarkus 3.17.4 → 3.18.2.

## [v1.0.2] — 2024-12-16
### Changed
- Moved base package to `com.github.kaivu`.

## [v1.0.1] — 2024-12-14
### Added
- Cached message interpolation for i18n resource bundles.

## [v1.0.0] — 2024-12-14
### Added
- Initial public release: Quarkus reactive REST boilerplate with hexagonal/clean architecture,
  OpenTelemetry tracing, resource-bundle-based error responses correlated with the OTel trace ID,
  native-image build support, and Gradle build migration.
