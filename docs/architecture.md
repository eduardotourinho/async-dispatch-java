# Architecture

## System Overview

The application is composed of two logical modules that currently run inside a single Spring Boot process. The **Task Manager** exposes a REST API to accept tasks and query their status. The **Task Converter** is a background worker that consumes tasks from SQS, processes them, and publishes results back through a second queue.

### Component Overview

The diagram below shows all components within each module and how they relate to the external infrastructure.

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

## Task Processing Flow

This diagram shows the end-to-end lifecycle of a single task, from submission through processing to final status update.

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
