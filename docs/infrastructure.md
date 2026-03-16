# Infrastructure

Production infrastructure is managed with Terraform. The configuration lives in `infrastructure/terraform/`.

## Prerequisites

- Terraform >= 1.0
- AWS account with permissions to create SQS queues, RDS instances, and security groups
- An existing VPC with at least two subnets in different availability zones (required for the RDS subnet group)

## Resources Created

### SQS Queues

| Resource | Name | Notes |
|----------|------|-------|
| Main task queue | `{environment}-tasks.fifo` | FIFO, content-based deduplication, 4-day retention, 30s visibility timeout, long polling (10s), max 3 receive attempts before DLQ |
| Task results queue | `{environment}-task-results.fifo` | FIFO, content-based deduplication, 4-day retention |
| Tasks DLQ | `{environment}-tasks-dlq.fifo` | FIFO, 14-day retention |
| Task results DLQ | `{environment}-task-results-dlq.fifo` | FIFO, 14-day retention |

### RDS PostgreSQL

| Resource | Details |
|----------|---------|
| Instance ID | `{environment}-async-dispatch-db` |
| Engine | PostgreSQL 17 |
| Instance class | `db.t3.micro` (configurable) |
| Storage | 20 GB gp3, autoscales to 100 GB |
| Encryption | Enabled |
| Backups | Automated, 7-day retention |
| Monitoring | Performance Insights + CloudWatch logs |

## Deploying

### 1. Configure Variables

Either export environment variables:

```bash
export TF_VAR_aws_region="us-east-1"
export TF_VAR_aws_access_key="your-access-key"
export TF_VAR_aws_secret_key="your-secret-key"
export TF_VAR_aws_account_id="123456789012"
export TF_VAR_environment="prod"
export TF_VAR_vpc_id="vpc-xxxxxxxxxxxxxxxxx"
export TF_VAR_db_subnet_ids='["subnet-xxxxxxxxxxxxxxxxx", "subnet-yyyyyyyyyyyyyyyyy"]'
export TF_VAR_db_username="postgres"
export TF_VAR_db_password="your-secure-password"
```

Or create a `terraform.tfvars` file (it is gitignored):

```bash
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your values
```

### 2. Apply

```bash
cd infrastructure/terraform
terraform init
terraform plan
terraform apply
```

### 3. Destroy

```bash
terraform destroy
```

## Outputs

After `terraform apply` completes, the following values are available via `terraform output`:

| Output | Description |
|--------|-------------|
| `rds_endpoint` | RDS endpoint in `host:port` format |
| `rds_address` | RDS hostname only |
| `rds_port` | RDS port (5432) |
| `rds_database_name` | Database name |
| `rds_instance_id` | RDS instance identifier |

Use `rds_endpoint` and the queue URLs to populate `application.properties` for production. See [Configuration](configuration.md#production-overrides) for details.
