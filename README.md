# Spring Boot 4 Workflow Client

Spring Boot 4 / Java 21 workflow client with a pluggable workflow abstraction and a Camunda 7 REST implementation backed by `WebClient`.

## Features

- Profiles: `local`, `development`, `test`, `staging`, `production`
- Default workflow engine: `camunda`
- Remote Camunda 7 REST integration only, no embedded Camunda engine
- WebClient timeouts and optional basic authentication
- Process, task, user/group, deployment, history, and external-task APIs
- BPMN auto deployment from `src/main/resources/processes/**/*.bpmn`
- Centralized exception handling with standard error responses
- Correlation ID and execution-time logging
- Docker and Docker Compose support

## Run Locally

This machine defaults to Java 8, so use Java 21 when running Gradle. If `JAVA_HOME` is not already pointing at a Java 21 installation, set it first and then run:

```bash
./gradlew bootRun
```

The app runs on `http://localhost:8081`.

Health:

```bash
curl http://localhost:8081/actuator/health
```

Swagger UI:

```text
http://localhost:8081/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8081/v3/api-docs
```

## Profiles

Shared defaults live in `src/main/resources/application.yml`.

Profile files:

- `application-local.yml`
- `application-development.yml`
- `application-test.yml`
- `application-staging.yml`
- `application-production.yml`

Camunda is enabled by default:

```yaml
camunda:
  enabled: true
  base-url: http://localhost:8080/engine-rest
  username:
  password:
  connect-timeout: 5s
  read-timeout: 30s
```

Override for remote environments:

```bash
SPRING_PROFILES_ACTIVE=production \
CAMUNDA_BASE_URL=https://camunda.example.com/engine-rest \
CAMUNDA_USERNAME=my-user \
CAMUNDA_PASSWORD=my-password \
java -jar build/libs/camunda-client-0.0.1-SNAPSHOT.jar
```

## Build

```bash
./gradlew clean build
```

## Test

```bash
./gradlew test
```

## Package

```bash
./gradlew bootJar
```

## Docker

```bash
./gradlew clean bootJar
docker compose up --build
```

## Example Process Start

Compatibility endpoint:

```bash
curl -X POST http://localhost:8081/api/process/start/sampleProcess \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: demo-123" \
  -d '{"variables":{"orderId":"123","amount":5000}}'
```

Workflow endpoint:

```bash
curl -X POST http://localhost:8081/api/workflow/processes/start/sampleProcess \
  -H "Content-Type: application/json" \
  -d '{"variables":{"orderId":"123","amount":5000},"businessKey":"order-123"}'
```

Example response:

```json
{
  "id": "process-instance-id",
  "definitionId": "sampleProcess:1:definition-id",
  "businessKey": "order-123",
  "ended": false,
  "suspended": false,
  "tenantId": null,
  "raw": {}
}
```

## Standard Error Response

```json
{
  "timestamp": "2026-05-28T11:30:00Z",
  "status": 502,
  "errorCode": "WORKFLOW_CONNECTION_ERROR",
  "message": "Could not connect to workflow engine",
  "path": "/api/workflow/processes/start/sampleProcess"
}
```

## BPMN Auto Deployment

At startup the app scans:

```text
classpath*:processes/**/*.bpmn
```

For each BPMN file it:

1. Computes a local SHA-256 checksum.
2. Finds the latest deployment with the generated deployment name.
3. Reads the deployed resource bytes from Camunda.
4. Skips deployment when checksums match.
5. Creates a new Camunda deployment when the BPMN is new or changed.

## Main API Groups

- Processes: `/api/workflow/processes`
- Tasks: `/api/workflow/tasks`
- Users and groups: `/api/workflow/users`, `/api/workflow/groups`
- Deployments: `/api/workflow/deployments`
- History: `/api/workflow/history`
- External tasks: `/api/workflow/external-tasks`
- Legacy compatibility: `/api/process/start/{key}`
- Actuator health/info: `/actuator/health`, `/actuator/info`

## Project Structure

```text
src/main/java/com/example/camunda_client
├── logging
└── workflow
    ├── api
    ├── camunda
    ├── config
    ├── controller
    ├── deployment
    ├── dto
    ├── exception
    ├── factory
    └── service
```
