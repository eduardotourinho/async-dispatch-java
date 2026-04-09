# Configuration

Both services are configured entirely through environment variables. The `application.properties` in each module defines defaults suitable for local development; override them in production via Kubernetes ConfigMaps and Secrets (see [Infrastructure](infrastructure.md)).

## task-manager

### Database

| Environment Variable | Default (local) | Description |
|---------------------|-----------------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/async_dispatch` | JDBC connection URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |

### Schema Migrations (Flyway)

Flyway is enabled by default (`spring.flyway.enabled=true`). On startup, task-manager applies any pending migrations from `src/main/resources/db/migration/`. The initial migration (`V1__init_schema.sql`) creates the `tasks` and `task_results` tables.

`hibernate.ddl-auto` is not set — schema management is handled exclusively by Flyway.

### AWS / SQS

| Environment Variable | Default (local) | Description |
|---------------------|-----------------|-------------|
| `AWS_REGION` | `us-east-1` | AWS region |
| `AWS_ENDPOINT_URL` | _(empty)_ | Set to `http://localhost:4566` for LocalStack. When empty, the service uses `DefaultCredentialsProvider` (IRSA in production). |
| `AWS_ACCESS_KEY_ID` | _(empty)_ | Only used when `AWS_ENDPOINT_URL` is set |
| `AWS_SECRET_ACCESS_KEY` | _(empty)_ | Only used when `AWS_ENDPOINT_URL` is set |
| `SQS_TASKS_QUEUE_URL` | `http://localhost:4566/000000000000/tasks.fifo` | Tasks queue URL |
| `SQS_TASK_RESULTS_QUEUE_URL` | `http://localhost:4566/000000000000/task-results.fifo` | Results queue URL |
| `SQS_TASKS_DLQ_QUEUE_URL` | `http://localhost:4566/000000000000/tasks-dlq.fifo` | Tasks DLQ URL |
| `SQS_TASK_RESULTS_DLQ_QUEUE_URL` | `http://localhost:4566/000000000000/task-results-dlq.fifo` | Results DLQ URL |

## task-worker

task-worker has no database. Its environment variables are a subset of the above (SQS only):

| Environment Variable | Default (local) | Description |
|---------------------|-----------------|-------------|
| `AWS_REGION` | `us-east-1` | AWS region |
| `AWS_ENDPOINT_URL` | _(empty)_ | LocalStack endpoint (local only) |
| `AWS_ACCESS_KEY_ID` | _(empty)_ | Local only |
| `AWS_SECRET_ACCESS_KEY` | _(empty)_ | Local only |
| `SQS_TASKS_QUEUE_URL` | `http://localhost:4566/000000000000/tasks.fifo` | Queue to consume from |
| `SQS_TASK_RESULTS_QUEUE_URL` | `http://localhost:4566/000000000000/task-results.fifo` | Queue to publish results to |
| `SQS_TASKS_DLQ_QUEUE_URL` | `http://localhost:4566/000000000000/tasks-dlq.fifo` | DLQ URL (for logging) |
| `SQS_TASK_RESULTS_DLQ_QUEUE_URL` | `http://localhost:4566/000000000000/task-results-dlq.fifo` | Results DLQ URL (for logging) |

## Actuator (both services)

Both services expose `/actuator/health/liveness` and `/actuator/health/readiness` — these are wired to the Kubernetes liveness and readiness probes in the Helm charts.

| Endpoint | URL |
|----------|-----|
| Health | `/actuator/health` |
| Liveness probe | `/actuator/health/liveness` |
| Readiness probe | `/actuator/health/readiness` |
| Metrics | `/actuator/metrics` |
| Info | `/actuator/info` |

## Springdoc / OpenAPI (task-manager only)

| Path | Description |
|------|-------------|
| `/api-docs` | OpenAPI JSON spec |
| `/swagger-ui.html` | Swagger UI |

## Production Overrides

In production (EKS), environment variables are injected via Kubernetes:

- Non-sensitive values (queue URLs, region, DB URL, DB username) come from a **ConfigMap** rendered by the Helm chart.
- Sensitive values (`DB_PASSWORD`) come from a **Kubernetes Secret** referenced in the Deployment.
- AWS credentials are **not set** — the pod uses its IRSA `ServiceAccount` instead. The `AwsConfig` bean detects the absence of `AWS_ENDPOINT_URL` and falls back to `DefaultCredentialsProvider`, which picks up the projected service account token automatically.

See the Helm values files (`helm/task-manager/values-prod.yaml`, `helm/task-worker/values-prod.yaml`) for production queue URLs and IRSA role ARNs.
