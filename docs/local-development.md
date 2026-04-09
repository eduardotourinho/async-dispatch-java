# Local Development

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Docker & Docker Compose | Latest stable |
| Gradle | 8.x (or use the included `./gradlew` wrapper) |

## Project Structure

This is a multi-module Gradle monorepo. Each service is an independent subproject:

```
async-dispatch/
├── common/          # Shared library (message DTOs, TaskType)
├── task-manager/    # REST API service
└── task-worker/     # SQS worker service
```

## Building

Build all modules:

```bash
./gradlew build
```

Build a specific module:

```bash
./gradlew :task-manager:build
./gradlew :task-worker:build
```

Run tests:

```bash
./gradlew :task-manager:test :task-worker:test
```

## Running with Docker Compose

The `docker-compose.yml` at the project root starts the full stack — both services, PostgreSQL, and LocalStack:

```bash
docker-compose up -d
```

This brings up:

| Container | Port | Purpose |
|-----------|------|---------|
| `async-dispatch-postgres` | `5432` | PostgreSQL database |
| `localstack-sqs` | `4566` | LocalStack (SQS emulation) |
| `async-dispatch-task-manager` | `8080` | task-manager REST API |
| `async-dispatch-task-worker` | `8081` | task-worker (SQS consumer) |

On startup, the LocalStack init script (`infrastructure/localstack/init-sqs.sh`) automatically creates all four required SQS FIFO queues.

## Running Services Locally (without Docker)

Start the infrastructure first:

```bash
docker-compose up -d postgres localstack
```

Then run each service with the appropriate environment variables:

**task-manager:**
```bash
AWS_ENDPOINT_URL=http://localhost:4566 \
AWS_ACCESS_KEY_ID=test \
AWS_SECRET_ACCESS_KEY=test \
DB_URL=jdbc:postgresql://localhost:5432/async_dispatch \
DB_USERNAME=postgres \
DB_PASSWORD=postgres \
./gradlew :task-manager:bootRun
```

**task-worker** (separate terminal):
```bash
AWS_ENDPOINT_URL=http://localhost:4566 \
AWS_ACCESS_KEY_ID=test \
AWS_SECRET_ACCESS_KEY=test \
./gradlew :task-worker:bootRun
```

## Verifying the Setup

Check that task-manager is healthy:

```bash
curl http://localhost:8080/actuator/health
```

Check that task-worker is healthy:

```bash
curl http://localhost:8081/actuator/health
```

Both should return `{ "status": "UP" }`.

Browse the API at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

## LocalStack SQS Details

### Queues Created Automatically

| Queue | Purpose | DLQ |
|-------|---------|-----|
| `tasks.fifo` | Incoming tasks from task-manager | `tasks-dlq.fifo` |
| `task-results.fifo` | Processed results from task-worker | `task-results-dlq.fifo` |
| `tasks-dlq.fifo` | Failed task messages (max 3 receive attempts) | — |
| `task-results-dlq.fifo` | Failed result messages (max 3 receive attempts) | — |

All queues use content-based deduplication.

### Verifying Queue Creation

```bash
aws --endpoint-url=http://localhost:4566 sqs list-queues
```

### Sending a Test Message Manually

```bash
aws --endpoint-url=http://localhost:4566 sqs send-message \
  --queue-url http://localhost:4566/000000000000/tasks.fifo \
  --message-body '{"type":"CONVERT_CURRENCY","taskId":"00000000-0000-0000-0000-000000000001","payload":{"amount":100,"fromCurrency":"EUR","toCurrency":"USD"}}' \
  --message-group-id test
```

### Reinitialising Queues

```bash
docker-compose down
docker-compose up -d postgres localstack
```
