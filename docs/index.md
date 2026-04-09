# Async Dispatch – Distributed Task Processing

A Spring Boot system demonstrating scalable asynchronous task processing using AWS SQS FIFO queues and PostgreSQL, structured as independently deployable microservices.

## Services

| Service | Responsibility |
|---------|---------------|
| **task-manager** | REST API for task submission and status retrieval; persists task state to PostgreSQL via Flyway-managed schema |
| **task-worker** | SQS consumer; processes tasks (currency conversion, interest calculation) and publishes results back via a second queue |
| **common** | Shared library — message DTOs and domain types consumed by both services |

See [Architecture](architecture.md) for how the services interact.

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 3.5.10 |
| SQS integration | Spring Cloud AWS 3.1.1 |
| Database | PostgreSQL 17 |
| Schema migrations | Flyway |
| ORM | Hibernate 6.6 |
| Local AWS | LocalStack |
| Container orchestration | Kubernetes (Helm + Kustomize) |
| Autoscaling | KEDA (SQS queue-depth) + HPA (CPU) |
| Infrastructure | Terraform |
| Runtime | Java 21 |
