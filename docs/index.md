# Async Dispatch – Distributed Task Processing

A Spring Boot application demonstrating scalable asynchronous task processing using AWS SQS FIFO queues and PostgreSQL, with LocalStack for local development.

## Architecture

The system has two logical modules running in a single application:

- **Task Manager** – REST API for task submission and status retrieval
- **Task Converter** – Worker service that consumes tasks from SQS queues

```mermaid
sequenceDiagram
    participant Client
    participant REST API
    participant PostgreSQL
    participant tasks.fifo
    participant Task Worker
    participant task-results.fifo
    participant Result Listener

    Client->>REST API: POST /api/tasks
    REST API->>PostgreSQL: Save task (PENDING)
    REST API->>tasks.fifo: Publish task message
    REST API-->>Client: 202 { taskId }

    tasks.fifo->>Task Worker: Consume message
    Note over Task Worker: ConvertCurrencyService<br/>or CalculateInterestService
    Task Worker->>task-results.fifo: Publish result message
    tasks.fifo-->>tasks-dlq.fifo: Failed messages (DLQ)

    task-results.fifo->>Result Listener: Consume result
    Result Listener->>PostgreSQL: Update task status (COMPLETED)
    task-results.fifo-->>task-results-dlq.fifo: Failed messages (DLQ)

    Client->>REST API: GET /api/tasks/{taskId}
    REST API->>PostgreSQL: Fetch task
    REST API-->>Client: 200 { status, result }
```

### Component Overview

```mermaid
graph TB
    Client(["HTTP Client"])

    subgraph TM ["Task Manager"]
        TC["TaskController"]
        TMgr["TaskManager"]
        TS["TaskService"]
        TP["TaskProducer"]
        TRL["TaskResultsSqsListener"]
        TDL["TasksDlqListener"]
        TRDL["TaskResultsDlqListener"]
    end

    subgraph TC2 ["Task Converter"]
        TWL["TaskWorkerSqsListener"]
        CCS["ConvertCurrencyService"]
        CIS["CalculateInterestService"]
        TRP["TaskResultProducer"]
    end

    subgraph SQS ["AWS SQS"]
        TQ[/"tasks.fifo"/]
        TRQ[/"task-results.fifo"/]
        TDLQ[/"tasks-dlq.fifo"/]
        TRDLQ[/"task-results-dlq.fifo"/]
    end

    DB[("PostgreSQL")]

    Client -->|"REST"| TC
    TC --> TMgr
    TMgr --> TS
    TMgr --> TP
    TS <--> DB

    TP -->|publish| TQ
    TQ -.->|"max retries exceeded"| TDLQ

    TQ -->|consume| TWL
    TWL --> CCS
    TWL --> CIS
    CCS --> TRP
    CIS --> TRP
    TRP -->|publish| TRQ
    TRQ -.->|"max retries exceeded"| TRDLQ

    TRQ -->|consume| TRL
    TRL --> TS

    TDLQ -->|consume| TDL
    TDL --> TS

    TRDLQ -->|consume| TRDL
```

## Tech stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 3.4.5 |
| SQS integration | Spring Cloud AWS 3.1.1 |
| Database | PostgreSQL 17 |
| ORM | Hibernate 6.6 |
| Local AWS | LocalStack |
| Infrastructure | Terraform |
| Runtime | Java 21 |

## Running locally

```bash
# Start PostgreSQL + LocalStack SQS
docker-compose up -d

# Run the application
./gradlew bootRun
```

## Supported task types

### `convert_currency`
Converts an amount between currencies using mock exchange rates (EUR, USD, GBP).

```json
{
  "type": "convert_currency",
  "payload": { "amount": 100.00, "fromCurrency": "EUR", "toCurrency": "USD" }
}
```

### `calculate_interest`
Calculates simple interest: `Interest = Principal × Rate × (Days / 365)`.

```json
{
  "type": "calculate_interest",
  "payload": { "principal": 1000.00, "annualRate": 5.5, "days": 90 }
}
```

## Known limitations & future improvements

- Both modules run in the same process — ideally they should be split for independent scaling
- No retry mechanism: failed tasks are lost
- Authorization/authentication should be handled at the platform level, not per-service
- Terraform templates should include guard-rails (region constraints, DB version policies)
