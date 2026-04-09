# Infrastructure

Production infrastructure has two layers: **Terraform** provisions AWS resources (SQS, RDS), and **Helm + Kustomize** manages the Kubernetes workloads.

---

## Terraform (AWS Resources)

Configuration lives in `infrastructure/terraform/`.

### Prerequisites

- Terraform >= 1.0
- AWS account with permissions to create SQS queues, RDS instances, and security groups
- An existing VPC with at least two subnets in different availability zones

### Resources Created

#### SQS Queues

| Resource | Name | Notes |
|----------|------|-------|
| Main task queue | `{environment}-tasks.fifo` | FIFO, content-based deduplication, 4-day retention, 30s visibility timeout, long polling (10s), max 3 retries before DLQ |
| Task results queue | `{environment}-task-results.fifo` | FIFO, content-based deduplication, 4-day retention |
| Tasks DLQ | `{environment}-tasks-dlq.fifo` | FIFO, 14-day retention |
| Task results DLQ | `{environment}-task-results-dlq.fifo` | FIFO, 14-day retention |

#### RDS PostgreSQL

| Resource | Details |
|----------|---------|
| Engine | PostgreSQL 17 |
| Instance class | `db.t3.micro` (configurable) |
| Storage | 20 GB gp3, autoscales to 100 GB |
| Encryption | Enabled |
| Backups | Automated, 7-day retention |
| Monitoring | Performance Insights + CloudWatch logs |

### Deploying

```bash
cd infrastructure/terraform

# Copy and fill in the variables file
cp terraform.tfvars.example terraform.tfvars

terraform init
terraform plan
terraform apply
```

### Outputs

| Output | Description |
|--------|-------------|
| `rds_endpoint` | RDS endpoint (`host:port`) — use as `DB_URL` |
| `tasks_queue_url` | URL of the tasks FIFO queue |
| `task_results_queue_url` | URL of the task-results FIFO queue |

---

## Kubernetes (Helm + Kustomize)

### Helm Charts

Each service has its own Helm chart:

| Chart | Path | Key templates |
|-------|------|--------------|
| task-manager | `helm/task-manager/` | Deployment, Service, ConfigMap, ServiceAccount (IRSA), HPA, PodDisruptionBudget |
| task-worker | `helm/task-worker/` | Deployment, ConfigMap, ServiceAccount (IRSA), KEDA ScaledObject, PodDisruptionBudget |

Environment-specific values files:

```
helm/task-manager/
├── values.yaml          # defaults
├── values-dev.yaml      # local/dev overrides
└── values-prod.yaml     # production overrides

helm/task-worker/
├── values.yaml
├── values-dev.yaml
└── values-prod.yaml
```

Render a chart to inspect the output before applying:

```bash
helm template task-manager helm/task-manager -f helm/task-manager/values-prod.yaml
helm template task-worker  helm/task-worker  -f helm/task-worker/values-prod.yaml
```

Lint:

```bash
helm lint helm/task-manager
helm lint helm/task-worker
```

### Kustomize Overlays

Overlays at `k8s/overlays/{dev,prod}` use Kustomize's `helmCharts` generator to render the Helm charts and apply environment-specific patches:

```bash
# Preview rendered manifests
kubectl apply -k k8s/overlays/dev  --dry-run=client
kubectl apply -k k8s/overlays/prod --dry-run=client

# Apply
kubectl apply -k k8s/overlays/prod
```

### KEDA (task-worker autoscaling)

task-worker scales based on SQS queue depth, not CPU. The KEDA `ScaledObject` in `helm/task-worker/templates/keda-scaledobject.yaml` polls `tasks.fifo` every 15 seconds and adds one pod replica per `targetQueueLength` messages queued.

| Parameter | Dev | Prod |
|-----------|-----|------|
| `minReplicas` | 1 | 2 |
| `maxReplicas` | 10 | 50 |
| `targetQueueLength` | 5 | 5 |

KEDA must be installed in the cluster before deploying task-worker:

```bash
helm repo add kedacore https://kedacore.github.io/charts
helm install keda kedacore/keda --namespace keda --create-namespace
```

### IRSA (IAM Roles for Service Accounts)

Neither service uses hardcoded AWS credentials in production. Each `ServiceAccount` is annotated with an IAM role ARN:

```yaml
annotations:
  eks.amazonaws.com/role-arn: arn:aws:iam::123456789012:role/task-manager-irsa-role
```

The required IAM permissions per service:

| Service | Permissions |
|---------|-------------|
| task-manager | `sqs:SendMessage`, `sqs:ReceiveMessage`, `sqs:DeleteMessage`, `sqs:GetQueueAttributes` |
| task-worker | `sqs:ReceiveMessage`, `sqs:DeleteMessage`, `sqs:SendMessage`, `sqs:GetQueueAttributes` |

Set the role ARN in the production values file:

```yaml
# helm/task-manager/values-prod.yaml
serviceAccount:
  irsaRoleArn: "arn:aws:iam::123456789012:role/task-manager-irsa-role"
```
