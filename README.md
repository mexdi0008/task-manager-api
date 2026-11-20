# Task Manager API

A clean, production-style REST API for personal task management, built with Spring Boot 3 and secured with stateless JWT authentication.

![CI](../../actions/workflows/ci.yml/badge.svg) ![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-brightgreen) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue) ![Docker](https://img.shields.io/badge/Docker-ready-2496ED)

## Highlights

- **JWT authentication** — register / login, BCrypt password hashing, stateless security filter chain
- **Ownership enforced in the data layer** — every query is scoped by owner (`findByIdAndOwnerId`), so users can never read or change someone else's tasks
- **Validation & error handling** — Bean Validation on all inputs, RFC 7807 `ProblemDetail` error responses with per-field messages
- **Pagination, sorting & filtering** — `GET /api/v1/tasks?status=DONE&page=0&size=20&sort=dueDate,asc`
- **OpenAPI / Swagger UI** — interactive docs at `/swagger-ui.html`
- **Tested** — integration tests (MockMvc + H2) and unit tests for the JWT service
- **Containerized** — multi-stage Dockerfile (non-root runtime) and Docker Compose with PostgreSQL
- **CI** — GitHub Actions runs the full test suite on every push

## API

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Create an account, returns a JWT |
| `POST` | `/api/v1/auth/login` | Log in, returns a JWT |
| `GET` | `/api/v1/tasks` | List own tasks (paging, sorting, `status` filter) |
| `GET` | `/api/v1/tasks/{id}` | Get a task |
| `POST` | `/api/v1/tasks` | Create a task |
| `PUT` | `/api/v1/tasks/{id}` | Update a task |
| `DELETE` | `/api/v1/tasks/{id}` | Delete a task |

### Example

```bash
curl -X POST localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Jane Doe","email":"jane@example.com","password":"Str0ngPass!"}'

curl -X POST localhost:8080/api/v1/tasks \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"title":"Prepare demo","priority":"HIGH","dueDate":"2030-01-15"}'
```

Error response example:

```json
{
  "status": 400,
  "detail": "Validation failed",
  "errors": { "email": "must be a well-formed email address" }
}
```

## Running

### With Docker (recommended)
```bash
cp .env.example .env        # set DB_PASSWORD and JWT_SECRET (openssl rand -base64 48)
docker compose up --build
```

### Locally
```bash
export JWT_SECRET=$(openssl rand -base64 48)
./mvnw spring-boot:run      # expects PostgreSQL on localhost:5432/taskmanager
```

### Tests
```bash
./mvnw test
```

## Project structure

```
src/main/java/com/mehti/taskmanager
├── auth       # register / login endpoints and service
├── security   # JWT service, authentication filter, security configuration
├── task       # task entity, repository, service, controller, DTOs
├── user       # user entity and repository
└── common     # exceptions, global error handler, OpenAPI config
```

## Tech stack

Java 17 · Spring Boot 3.4 · Spring Security · Spring Data JPA · PostgreSQL · JJWT · springdoc-openapi · JUnit 5 · MockMvc · H2 · Docker · GitHub Actions
