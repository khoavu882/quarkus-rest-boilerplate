# Setup & Development

Operational guide: getting the service running locally, environment variables, build commands, and
troubleshooting. For what the codebase looks like and why, see [ARCHITECTURE.md](ARCHITECTURE.md).

## Prerequisites

- **Java 21+** — recommended via SDKMAN: `sdk env`
- **Docker & Docker Compose** — for the containerized local stack
- **Git**

## Local development options

### Option A — full Docker stack (simplest)

Builds the app and starts every dependency (Postgres, Redis, MinIO, Jaeger) in containers:

```bash
sdk env
./src/main/docker/scripts/local-setup.sh
```

The script validates your Java/Docker setup, builds the app, brings up all services, waits for each
to report healthy, then prints every service URL and follows the app's logs.

### Option B — native image stack (advanced)

Same as Option A but builds a GraalVM native executable first (5–15 minute build time, faster
startup/lower memory at runtime). Uses different ports to avoid clashing with a JVM stack running
alongside it:

```bash
sdk env
./src/main/docker/scripts/local-setup-native.sh
```

| | JVM stack | Native stack |
|---|---|---|
| App | `:8080` | `:8081` |
| Management | `:9000` | `:9001` |

Make sure `.dockerignore` includes `!build/*-runner` so Docker can see the native executable during
the image build.

### Option C — infra in Docker, app via hot reload

Starts only the infrastructure containers, letting you run the app locally with Quarkus's dev-mode
hot reload:

```bash
docker compose -f src/main/docker/docker-compose-dependencies.yml up -d
./gradlew quarkusDev
```

### Manual Docker Compose operations

```bash
docker compose -f src/main/docker/docker-compose.yml up -d --build   # start everything
docker compose -f src/main/docker/docker-compose.yml logs -f quarkus-app  # app logs
docker compose -f src/main/docker/docker-compose.yml down              # stop
docker compose -f src/main/docker/docker-compose.yml down -v           # stop + wipe volumes
```

## Build & test commands

```bash
sdk env                                    # required before any Gradle command

./gradlew quarkusDev                                          # dev mode, hot reload (needs infra running)
./gradlew build                                               # full build
./gradlew test                                                # runs the test suite (currently empty — see ARCHITECTURE.md)

./gradlew spotlessApply                                       # format — required before commit
./gradlew spotlessCheck                                       # format check (CI runs this)

./gradlew build -Dquarkus.package.native=true                 # native build, local GraalVM
./gradlew build -Dquarkus.native.container-build=true          # native build, container-based
```

## Environment variables

Set via `.env` (see `.env-template` for the full list) or exported directly; Quarkus's relaxed env
binding picks these up with no extra wiring:

| Variable | Purpose | Local default |
|---|---|---|
| `QUARKUS_APPLICATION_NAME` | App/image name | `demo-service` |
| `QUARKUS_MANAGEMENT_ENABLED` | Enable the management port | `false` |
| `QUARKUS_DATASOURCE_DB_KIND` / `_HOST` / `_PORT` / `_DB` / `_SCHEMA` / `_USERNAME` / `_PASSWORD` | Postgres connection | `postgresql` / `localhost` / `5432` / `db_local` / `sch_local` / `postgres` / `postgres` |
| `QUARKUS_HIBERNATE_ORM_DATABASE_DEFAULT_SCHEMA` | Hibernate default schema | `sch_local` |
| `QUARKUS_REDIS_HOSTS` / `_TIMEOUT` | Redis connection (shared by default + demo clients) | `redis://redis:6379` / `3s` |
| `QUARKUS_REST_CLIENT_DEMO_CLIENT_URL` / `_SCOPE` | Demo outbound REST client | — |
| `MINIO_URL` / `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | Object storage | `http://localhost:9001` / `minioadmin` / `minioadmin` |
| `QUARKUS_NATIVE_ADDITIONAL_BUILD_ARGS` / `QUARKUS_NATIVE_CONTAINER_BUILD` | Native build tuning | — |

Application-specific tuning (cache TTLs, retry backoff, timeouts, health thresholds — see
`AppConfiguration` in ARCHITECTURE.md) is under the `app.*` namespace in `application.yml`; every
property there follows the same `APP_<SECTION>_<KEY>` env var convention, e.g.
`APP_CACHE_ENTITY_DEVICE_TTL_MS`.

## Service URLs (Docker stack)

| Service | URL / Address | Notes |
|---|---|---|
| Quarkus app | http://localhost:8080 | |
| Swagger UI | http://localhost:8080/q/swagger-ui | |
| Health | http://localhost:8080/q/health | |
| Metrics (Prometheus) | http://localhost:8080/q/metrics | |
| Management | http://localhost:9000/q/health | when `QUARKUS_MANAGEMENT_ENABLED=true` |
| PostgreSQL | `localhost:5432` | `postgres` / `postgres`, db `db_local`, schema `sch_local` |
| Redis | `localhost:6379` | |
| MinIO API | http://localhost:9090 | |
| MinIO Console | http://localhost:9091 | `minioadmin` / `minioadmin` |
| Jaeger UI | http://localhost:16686 | |

## Troubleshooting

**Port conflicts** — check what's already bound before starting the stack:
```bash
lsof -i :8080  # app
lsof -i :5432  # PostgreSQL
lsof -i :6379  # Redis
lsof -i :9090  # MinIO API / :9091 console
lsof -i :9000  # management port
```

**Wrong Java version picked up by Gradle** — the daemon caches the JDK it started with; switching
via `sdk env` doesn't affect an already-running daemon:
```bash
java -version
sdk env
./gradlew --stop   # forces the next invocation to pick up the new JAVA_HOME
```

**Docker resource/state issues**:
```bash
docker ps
docker compose version
docker system df
docker system prune
```

**Inspecting logs**:
```bash
docker compose -f src/main/docker/docker-compose.yml logs -f            # everything
docker compose -f src/main/docker/docker-compose.yml logs -f quarkus-app # app only
docker compose -f src/main/docker/docker-compose.yml ps                  # health status
```
