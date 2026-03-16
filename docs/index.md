# Async Dispatch – Distributed Task Processing

A Spring Boot application demonstrating scalable asynchronous task processing using AWS SQS FIFO queues and PostgreSQL, with LocalStack for local development.

## Modules

The system has two logical modules running in a single application:

- **Task Manager** – REST API for task submission and status retrieval
- **Task Converter** – Worker service that consumes tasks from SQS queues

See [Architecture](architecture.md) for a detailed overview of how these modules interact.

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 3.5.10 |
| SQS integration | Spring Cloud AWS 3.1.1 |
| Database | PostgreSQL 17 |
| ORM | Hibernate 6.6 |
| Local AWS | LocalStack |
| Infrastructure | Terraform |
| Runtime | Java 21 |

## Known Limitations

- Both modules run in the same process — ideally they should be split for independent scaling
- No retry mechanism: failed tasks are lost
- Authorization/authentication should be handled at the platform level, not per-service
- Terraform templates should include guard-rails (region constraints, DB version policies)
