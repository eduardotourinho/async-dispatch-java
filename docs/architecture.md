# Architecture

## System Overview

The system is split into two independently deployable services backed by a shared library.

- **task-manager** — REST API, PostgreSQL persistence, SQS producer, result and DLQ listeners
- **task-worker** — SQS consumer, task processors, result producer
- **common** — shared JAR: `TaskType`, `ConvertCurrencyMessage`, `CalculateInterestMessage`, `TaskResultMessage`

### Component Overview

The diagram below shows all components within each service and how they relate to the external infrastructure.

![component_architecture.svg](component_architecture.svg)

## Task Processing Flow

This diagram shows the end-to-end lifecycle of a single task, from submission through processing to final status update.

![task_sequence_diagram.svg](task_sequence_diagram.svg)

## Message Contract

Both services communicate exclusively through SQS FIFO queues — there is no direct service-to-service call.

| Queue | Producer | Consumer | DLQ |
|-------|----------|----------|-----|
| `tasks.fifo` | task-manager | task-worker | `tasks-dlq.fifo` |
| `task-results.fifo` | task-worker | task-manager | `task-results-dlq.fifo` |

All task messages carry a `taskId` (UUID) and a `type` field. The `tasks-dlq.fifo` listener in task-manager marks the corresponding task as `CANCELLED` after the SQS maximum receive count (3) is exhausted.

## Kubernetes Deployment

In production, both services run on EKS with the following characteristics:

### task-manager
- **Scaling:** `HorizontalPodAutoscaler` (CPU utilisation, 70% target, 2–20 replicas)
- **HA:** `PodDisruptionBudget` (`minAvailable: 2` in production), `topologySpreadConstraints` across nodes
- **Auth:** `ServiceAccount` annotated with an IRSA role ARN — no hardcoded AWS credentials

### task-worker
- **Scaling:** KEDA `ScaledObject` driven by SQS queue depth — one additional pod per 5 messages queued (1–50 replicas in production). Scales to zero when the queue is empty.
- **HA:** `PodDisruptionBudget` (`minAvailable: 1`), `topologySpreadConstraints`
- **Auth:** Same IRSA pattern; KEDA uses `identityOwner: pod` to read queue depth without a separate AWS secret

### GitOps
Helm charts live in `helm/task-manager` and `helm/task-worker`. Kustomize overlays at `k8s/overlays/{dev,prod}` render the charts and apply environment-specific patches, following the pattern used by ArgoCD and Flux.
