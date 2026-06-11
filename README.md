# YAML Microservice

A Spring Boot 3 microservice that reads a structured YAML file and exposes its
key-value pairs through a secured REST API. All endpoints (except login) are
protected with JWT Bearer authentication.

---

## Features

| Feature | Detail |
|---|---|
| YAML reading | Reads `AppValues` YAML file on every request |
| Validation | Strict structural and type validation of YAML content |
| JWT auth | Stateless Bearer-token security via JJWT 0.12 |
| Role-based access | `USER` and `ADMIN` roles; reload endpoint is ADMIN-only |
| Error handling | Centralised `GlobalExceptionHandler` with JSON error bodies |
| Docker | Multi-stage `Dockerfile` + `docker-compose.yml` |
| Tests | Full integration test suite via MockMvc |

---

## YAML Format

The service expects a YAML file whose root key is **exactly** `AppValues`,
followed by a flat map of string key-value pairs:

```yaml
AppValues:
  app.name: MyApp
  app.version: 1.0.0
  db.host: localhost
  feature.darkMode: "true"
```

### Validation rules

1. The root key must be `AppValues` (case-sensitive).
2. `AppValues` must contain **at least one** key-value pair.
3. All keys must be non-null strings.
4. All values must be scalar (string/number/boolean) — nested maps or lists are rejected.

---

## API Reference

### `POST /api/auth/login`

Obtain a JWT. This endpoint is **public** (no token required).

**Request body:**
```json
{ "username": "admin", "password": "admin123" }
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "expiresIn": 3600000
}
```

Use the token in the `Authorization` header of subsequent requests:
```
Authorization: Bearer <token>
```

---

### `GET /api/values`

Return all key-value pairs.

**Roles:** `USER`, `ADMIN`

**Response:**
```json
{
  "app.name": "YamlMicroservice",
  "app.version": "1.0.0",
  "db.host": "localhost"
}
```

---

### `GET /api/values/{key}`

Return a single value by key.

**Roles:** `USER`, `ADMIN`

**Example:**
```
GET /api/values/app.name
```
```json
{ "app.name": "YamlMicroservice" }
```

Returns `422 Unprocessable Entity` if the key does not exist.

---

### `GET /api/values/reload`

Force a re-read and re-validation of the YAML file.

**Roles:** `ADMIN` only

**Response:**
```json
{ "status": "ok", "entriesLoaded": 10 }
```

---

## Quick Start (Local)

### Prerequisites

- Java 17+
- Maven 3.9+ (or use the included `./mvnw` wrapper)

```bash
# 1. Clone / unzip the project
cd spring-yaml-service

# 2. Run the application
./mvnw spring-boot:run

# 3. Obtain a JWT
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 4. Use the token
TOKEN="<paste token here>"
curl http://localhost:8080/api/values \
  -H "Authorization: Bearer $TOKEN"
```

---

## Quick Start (Docker)

```bash
# Build and run with Docker Compose
docker compose up --build

# Or build the image manually
docker build -t yaml-service:latest .
docker run -p 8080:8080 yaml-service:latest
```

### Mount a custom YAML file

```bash
docker run -p 8080:8080 \
  -v /path/to/your/appvalues.yaml:/app/appvalues.yaml:ro \
  -e APP_YAML_FILE_PATH=appvalues.yaml \
  yaml-service:latest
```

---

## Configuration Reference

| Property | Default | Description |
|---|---|---|
| `app.yaml.file-path` | `appvalues.yaml` | Path to the YAML file (classpath or absolute) |
| `jwt.secret` | *(see below)* | HMAC-SHA signing secret — **change in production** |
| `jwt.expiration-ms` | `3600000` | Token validity in ms (default: 1 hour) |

### Environment variable overrides

Spring Boot maps environment variables to properties automatically:

| Env var | Maps to |
|---|---|
| `JWT_SECRET` | `jwt.secret` |
| `JWT_EXPIRATION_MS` | `jwt.expiration-ms` |
| `APP_YAML_FILE_PATH` | `app.yaml.file-path` |

---

## Running Tests

```bash
./mvnw test
```

Test coverage includes:

- Service unit tests (load, validate, get single key)
- Controller integration tests via MockMvc (auth flow, all endpoints, role enforcement)
- Error-case tests (bad credentials, missing key, wrong role)

---

## Project Structure

```
spring-yaml-service/
├── src/
│   ├── main/
│   │   ├── java/com/example/yamlservice/
│   │   │   ├── YamlServiceApplication.java        # Entry point
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java            # POST /api/auth/login
│   │   │   │   └── AppValuesController.java       # GET /api/values/**
│   │   │   ├── service/
│   │   │   │   └── AppValuesService.java          # YAML load + validation
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java          # Token generation/validation
│   │   │   │   ├── JwtAuthenticationFilter.java   # Request filter
│   │   │   │   └── SecurityConfig.java            # Spring Security setup
│   │   │   ├── model/
│   │   │   │   ├── AppValues.java                 # YAML data model
│   │   │   │   ├── AuthRequest.java               # Login request DTO
│   │   │   │   ├── AuthResponse.java              # JWT response DTO
│   │   │   │   └── ApiError.java                  # Error response body
│   │   │   └── exception/
│   │   │       ├── YamlValidationException.java
│   │   │       ├── YamlFileNotFoundException.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── appvalues.yaml                     # Sample YAML file
│   └── test/
│       └── java/com/example/yamlservice/
│           ├── AppValuesServiceTest.java
│           └── AppValuesControllerTest.java
├── Dockerfile                                      # Multi-stage build
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## Default Users

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | USER |

> **Production note:** Replace the in-memory user store in `SecurityConfig` with a
> database-backed `UserDetailsService` and store passwords as BCrypt hashes.
> Always override `jwt.secret` with a strong random value via the `JWT_SECRET`
> environment variable.

---

## Error Response Format

All errors return a consistent JSON body:

```json
{
  "status": 422,
  "error": "YAML Validation Error",
  "message": "Key 'missing.key' not found in AppValues",
  "path": "/api/values/missing.key",
  "timestamp": "2024-01-15T10:30:00Z",
  "details": null
}
```
