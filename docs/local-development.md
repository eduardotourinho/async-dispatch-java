# Local Development

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Docker & Docker Compose | Latest stable |
| Gradle | 8.x (or use the included `./gradlew` wrapper) |

## Starting the Infrastructure

The `docker-compose.yml` at the project root starts both PostgreSQL and LocalStack:

```bash
docker-compose up -d
```

This brings up:

- **PostgreSQL 17** on port `5432` — database used by the application
- **LocalStack** on port `4566` — local emulation of AWS SQS

On startup, the LocalStack init script (`infrastructure/localstack/init-sqs.sh`) automatically creates all four required SQS FIFO queues. No manual queue creation is needed.

## Running the Application

```bash
./gradlew bootRun
```

The application starts on port `8080` and initialises both the Task Manager and Task Converter modules.

## Verifying the Setup

Check that the application is healthy:

```bash
curl http://localhost:8080/actuator/health
```

A successful response looks like:

```json
{ "status": "UP" }
```

You can also view build and app info at:

```bash
curl http://localhost:8080/actuator/info
```

## LocalStack SQS Details

### Queues Created Automatically

| Queue | Purpose | DLQ |
|-------|---------|-----|
| `tasks.fifo` | Incoming tasks from Task Manager | `tasks-dlq.fifo` |
| `task-results.fifo` | Processed results from Task Converter | `task-results-dlq.fifo` |
| `tasks-dlq.fifo` | Failed task messages (max 3 receive attempts) | — |
| `task-results-dlq.fifo` | Failed result messages (max 3 receive attempts) | — |

All queues use content-based deduplication.

### Verifying Queue Creation

```bash
aws --endpoint-url=http://localhost:4566 sqs list-queues
```

You should see all four queue URLs in the response.

### Sending a Test Message Manually

```bash
aws --endpoint-url=http://localhost:4566 sqs send-message \
  --queue-url http://localhost:4566/000000000000/tasks.fifo \
  --message-body '{"type":"CONVERT_CURRENCY","payload":{"amount":100,"fromCurrency":"EUR","toCurrency":"USD"}}' \
  --message-group-id test
```

### Reinitialising Queues

If queues are missing or corrupted, recreate them by restarting LocalStack cleanly:

```bash
docker-compose down
rm -rf .localstack
docker-compose up -d
```

You can also check LocalStack logs to diagnose initialisation problems:

```bash
docker logs localstack-sqs
```
