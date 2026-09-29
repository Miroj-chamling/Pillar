# Finance App

A multi-account banking app for learning reactive Java end to end.

| Folder | What | Stack |
|---|---|---|
| `backend/` | REST API | Spring Boot 4 WebFlux, R2DBC, PostgreSQL, Flyway, Spring Security (JWT) |
| `frontend/` | Web UI | React 19, TypeScript, Vite, React Router, TanStack Query |
| `compose.yaml` | Local database | PostgreSQL 18 on port **5433** |

## Prerequisites

Java 17+, Node 20+, Docker (Desktop must be running).

## Run it locally

```bash
docker compose up -d                 # 1. database (from the repo root)

cd backend && ./mvnw spring-boot:run  # 2. API on http://localhost:8080 (applies migrations, creates the admin)

cd frontend && npm install && npm run dev   # 3. UI on http://localhost:5173
```

Open http://localhost:5173. The Vite dev server proxies `/api/*` to the backend, so no CORS setup is needed.

### Dev credentials

| Role | Email | Password |
|---|---|---|
| Admin | `admin@finance.local` | `Admin@12345` |
| Customer | register one on the sign-up page | |

Outside local dev, set `JWT_SECRET`, `ADMIN_EMAIL` and `ADMIN_PASSWORD` environment variables.

## Tests

```bash
cd backend && ./mvnw test     # integration tests against a throwaway Postgres (Testcontainers, needs Docker)
cd frontend && npm run build  # type-check + production build
cd frontend && npm run lint
```

## Database

```bash
docker compose exec postgres psql -U finance -d finance   # SQL shell
docker compose down -v                                    # wipe all data (fresh DB on next start)
```

Schema changes go in `backend/src/main/resources/db/migration/` as new `V<n>__description.sql` files. Never edit a migration that has already run.

## API

| Method | Path | Who |
|---|---|---|
| POST | `/api/auth/register` | public |
| POST | `/api/auth/login` | public |
| GET | `/api/me` | logged in |
| GET, POST | `/api/accounts` | customer |
| GET | `/api/accounts/{id}` | customer (own accounts only) |
| GET | `/api/admin/customers` | admin |
| GET, POST | `/api/admin/customers/{id}/accounts` | admin |

Errors are RFC 9457 Problem Details JSON; validation errors include an `errors: [{field, message}]` array.
