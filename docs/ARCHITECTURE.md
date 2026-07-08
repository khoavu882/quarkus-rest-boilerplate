# Architecture

This document describes how the codebase is organized and why, for anyone extending the boilerplate.
For "how do I run this," see [SETUP.md](SETUP.md). For the project overview, see the root
[README.md](../README.md).

## Layering (hexagonal / ports-and-adapters)

```
src/main/java/com/github/kaivu/
├── Application.java              # Entry point
├── adapter/
│   ├── in/
│   │   ├── rest/                 # JAX-RS resources, request DTOs, view models, validators
│   │   └── filter/                # Request filters (trace header propagation, etc.)
│   └── out/
│       ├── api/, client/          # Outbound REST/API clients (DemoClientService, MinioHelper, RedisHelper) + impl/
│       ├── persistence/            # Panache reactive repositories
│       ├── handler/                 # Client-side error enums (ClientErrorsEnum) + exception mappers
│       └── exception/               # Adapter-level exceptions
├── application/
│   ├── usecase/ + usecase/impl/    # Orchestration layer, called directly by REST resources
│   ├── service/ + service/impl/    # Business logic, one service per aggregate
│   ├── port/                        # Repository interfaces the application layer depends on
│   └── exception/                   # Application-level exceptions
├── domain/                          # JPA/Panache entities, enums, audit base class, custom Hibernate types
├── common/
│   ├── constant/, context/, exception/, mapper/, service/, utils/
└── config/                          # CDI producers, global exception handling, security, metrics, caching
```

Request flow: **REST resource → UseCase (orchestration) → Service (business rules) → Repository
(Panache reactive)**. Resources depend on UseCase interfaces only, never on Service or Repository
directly. `EntityDevicesResource` / `EntityDeviceUseCaseImpl` is the reference implementation new
features should follow the shape of.

Everything is reactive end to end: Mutiny `Uni`/`Multi`, Hibernate Reactive Panache (no blocking
JDBC), `@WithTransaction` on write methods in the use-case layer. Blocking third-party SDK calls
(e.g. the MinIO client) are explicitly offloaded to a virtual-thread executor rather than run on the
Vert.x event loop — see `MinioHelperImpl`.

## Error handling

Errors are isolated into three independent categories, each with its own exception hierarchy and
mapper chain:

1. **Business errors** — raised by this service's own logic. `ServiceException` and its subclasses
   (`EntityNotFoundException`, `EntityConflictException`, `NotAcceptableException`,
   `PermissionDeniedException`, `UnauthorizedException`) each wrap an `ErrorsEnum` constant, keyed as
   `entity.error_key` and resolved against the i18n bundles in `src/main/resources/i18n/`. Each
   subtype has a dedicated `ExceptionMapper` under `config/handler/mapper/`, plus
   `CompositeExceptionMapper` for Mutiny's `CompositeException` (used when a reactive pipeline
   surfaces more than one failure at once — JAX-RS's normal per-type mapper selection doesn't apply
   there, so it dispatches manually via an explicitly ordered handler table).
2. **External errors** — normalized from outbound REST client failures (e.g. `DemoClientService`),
   using the separate `ClientErrorsEnum`/`ClientException`/`ClientExceptionMapper` under
   `adapter/out/handler/`. Kept independent from the business-error enum so a client-integration
   failure never gets mis-reported as one of this service's own business rules.
3. **Runtime/unclassified errors** — anything that is neither of the above (a bug, an unanticipated
   exception from a library) is caught by `ErrorsHandler`, a catch-all `ExceptionMapper<Throwable>`.
   It logs the full exception with the request's trace ID server-side but never echoes exception
   internals to the client — only a generic, localized message.

`AppErrorEnum.getMessage(Locale, Object...)` is stateless: it resolves and formats the message fresh
on every call (cached by key+locale in a `ConcurrentHashMap`) rather than mutating the enum constant,
so concurrent requests can't clobber each other's error message. `ServiceException` carries its own
`locale`/`args` via `.withLocale(...)`/`.withArgs(...)`, set once per thrown instance. A missing i18n
key logs and falls back to the raw key rather than throwing — a bundle gap must never crash the
mapper that's trying to render the original error.

Pattern for adding a new error: add an `EntitiesConstant`/`ErrorsKeyConstant` pair if needed → add an
`ErrorsEnum` constant → add the key to all three `error_messages*.properties` files (default/en/vi) →
throw the matching exception with `.withLocale(languageContext.getCurrentLocale()).withArgs(...)`.

**Coupling to swap later:** `ServiceException`/`AppErrorEnum` resolve messages synchronously through
the static `ResourceBundleUtil`. If message resolution is ever moved to a remote/centralized config
service, the smallest seam is a `MessageResolver` interface sitting between `ErrorsEnum` and
`ResourceBundleUtil` — not making `getMessage` return `Uni<String>`, which would force reactive types
through `RuntimeException.getMessage()`, an API this codebase doesn't otherwise touch.

## Caching

`CacheService` (Redis-backed, cache-aside) is used from the use-case layer: read via
`getOrCompute`/`get`, write via `set` with an explicit TTL, and invalidate related keys — including
pattern-based `deleteByPattern` for list/page caches — on create/update/delete. Cache key
prefixes and TTLs are externalized under `app.cache.*` in `application.yml` (bound via
`AppConfiguration`, a type-safe `@ConfigMapping`), not hardcoded in the service classes.
`RedisHelperImpl` uses the non-blocking `SCAN` cursor for pattern-based key lookups, not `KEYS`,
which would otherwise block the whole Redis instance while iterating the keyspace.

## Observability

- **Metrics**: Micrometer, exported via the Prometheus registry at `/q/metrics`. `AppMetrics`
  registers both direct gauges (uptime, request/error totals, DB connection counts) and cache
  hit/miss/error counters (backed by `LongAdder`, wrapped in a Micrometer gauge) so every metric
  reaches the same pipeline. `MediaStreamingService` additionally registers its own
  `Counter`/`Timer` for streaming request volume and duration.
- **Tracing**: OpenTelemetry, exported to Jaeger in the local Docker stack. The trace ID from
  request headers (`AppHeaderConstant.TRACE_ID`) is echoed as `errorId` in every error response body
  so a client-reported error can be correlated back to a trace.
- **Logging**: structured console format includes `traceId`/`parentId`/`spanId`/`sampled` (see
  `quarkus.log.console.format` in `application.yml`). Exception mappers log the full throwable
  (not just `.getMessage()`) so stack traces survive into the logs.
- **Health**: SmallRye Health at `/q/health`, plus a custom `ObservabilityHealthCheck`.

## Configuration

Runtime config is env-var driven via Quarkus/SmallRye's relaxed binding (`EnvConfigSource`), which
maps any config property path to an env var automatically — `quarkus.management.enabled` accepts
`QUARKUS_MANAGEMENT_ENABLED` with no explicit `${...}` expression required. The explicit
`${ENV_VAR:default}` form in `application.yml` is used only where one env var intentionally feeds
more than one property path (e.g. the same Redis host shared by the default and `demo` named
clients) — automatic binding is strictly 1:1 per property path, so a shared variable needs the
expression written at each site.

Application-specific settings (cache TTLs, retry backoff, timeouts, health thresholds, pagination
headers, i18n paths) are externalized under the `app.*` namespace and bound to a type-safe
`AppConfiguration` `@ConfigMapping` interface, rather than read ad hoc via `@ConfigProperty` scattered
across classes.

## Known limitations

- **No automated tests yet.** `quarkus-junit5` and `quarkus-panache-mock` are on the test classpath,
  but there is no `src/test` directory — `./gradlew test` currently runs zero tests.
- **`@RolesAllowed`/the OpenAPI `jwt` security scheme are declarative only.** No identity provider
  (OIDC, JWT validation extension, etc.) is wired into `build.gradle`; the annotations document
  intent for consumers of the boilerplate to wire in their own auth, they don't enforce anything
  today.
- **`/stream` is HTTP range-request media streaming**, not WebSockets — despite the name, there is
  no `@ServerEndpoint`/WebSocket usage in the codebase.
