# Configuration

All configuration lives in `src/main/resources/application.properties`. The table below documents every key, its default value for local development, and what it controls.

## Database

| Property | Default | Description |
|----------|---------|-------------|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/async_dispatch` | JDBC connection URL for PostgreSQL |
| `spring.datasource.username` | `postgres` | Database username |
| `spring.datasource.password` | `postgres` | Database password |
| `spring.datasource.driver-class-name` | `org.postgresql.Driver` | JDBC driver class |

## JPA / Hibernate

| Property | Default | Description |
|----------|---------|-------------|
| `spring.jpa.hibernate.ddl-auto` | `update` | Schema management strategy. `update` applies incremental DDL changes on startup |
| `spring.jpa.show-sql` | `true` | Logs generated SQL statements to stdout |
| `spring.jpa.properties.hibernate.dialect` | `org.hibernate.dialect.PostgreSQLDialect` | Hibernate SQL dialect |
| `spring.jpa.properties.hibernate.format_sql` | `true` | Pretty-prints logged SQL |

## AWS / SQS

| Property | Default | Description |
|----------|---------|-------------|
| `spring.cloud.aws.region.static` | `us-east-1` | AWS region (static, no instance metadata lookup) |
| `spring.cloud.aws.credentials.access-key` | `test` | AWS access key — use `test` for LocalStack |
| `spring.cloud.aws.credentials.secret-key` | `test` | AWS secret key — use `test` for LocalStack |
| `spring.cloud.aws.endpoint` | `http://localhost:4566` | AWS SDK endpoint override; points to LocalStack locally. Remove for production |
| `spring.cloud.aws.sqs.enabled` | `true` | Enables the Spring Cloud AWS SQS integration |
| `spring.cloud.aws.sqs.endpoint` | `http://localhost:4566` | SQS-specific endpoint override for LocalStack |

## SQS Queue URLs

| Property | Default | Description |
|----------|---------|-------------|
| `spring.cloud.aws.sqs.tasks-queue-url` | `http://localhost:4566/000000000000/tasks.fifo` | URL of the main task processing queue |
| `spring.cloud.aws.sqs.task-results-queue-url` | `http://localhost:4566/000000000000/task-results.fifo` | URL of the task results queue |
| `spring.cloud.aws.sqs.tasks-dlq-queue-url` | `http://localhost:4566/000000000000/tasks-dlq.fifo` | URL of the tasks dead letter queue |
| `spring.cloud.aws.sqs.task-results-dlq-queue-url` | `http://localhost:4566/000000000000/task-results-dlq.fifo` | URL of the task results dead letter queue |

## Actuator

| Property | Default | Description |
|----------|---------|-------------|
| `management.endpoints.web.exposure.include` | `health,info,metrics,env` | Endpoints exposed over HTTP |
| `management.endpoint.health.show-details` | `always` | Shows full health component details |
| `management.info.env.enabled` | `true` | Exposes `info.*` properties via `/actuator/info` |
| `management.info.java.enabled` | `true` | Includes JVM version in `/actuator/info` |
| `management.info.build.enabled` | `true` | Includes build metadata in `/actuator/info` (populated by `springBoot.buildInfo()`) |
| `info.app.name` | `async-dispatch` | Application name shown in `/actuator/info` |
| `info.app.version` | `0.0.1` | Application version shown in `/actuator/info` |

## Springdoc / OpenAPI

| Property | Default | Description |
|----------|---------|-------------|
| `springdoc.api-docs.path` | `/api-docs` | Path for the OpenAPI JSON spec |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | Path for the Swagger UI |

## Miscellaneous

| Property | Default | Description |
|----------|---------|-------------|
| `spring.application.name` | `async-dispatch` | Application name reported in logs and actuator |
| `spring.docker.compose.enabled` | `false` | Disables Spring Boot's automatic Docker Compose lifecycle management |

## Production Overrides

For production, replace the LocalStack-specific values with real AWS endpoints and credentials (or use IAM instance profiles). At minimum, update:

- `spring.cloud.aws.credentials.access-key` / `secret-key` (or switch to `spring.cloud.aws.credentials.instance-profile=true`)
- Remove `spring.cloud.aws.endpoint` and `spring.cloud.aws.sqs.endpoint` to use real AWS endpoints
- Update all four SQS queue URLs with values from Terraform outputs
- Update `spring.datasource.url` with the RDS endpoint from Terraform outputs
