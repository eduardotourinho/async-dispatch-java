# Terraform Infrastructure

This Terraform configuration provisions AWS resources for production deployment including SQS queues and RDS PostgreSQL database.

## Prerequisites

- Terraform >= 1.0
- AWS Account with appropriate permissions
- AWS credentials (access key and secret key)
- VPC with at least 2 subnets in different availability zones (for RDS)

## Resources Created

### SQS Queues

- **Main Queue**: `{environment}-test-queue.fifo`
  - FIFO queue with content-based deduplication
  - Message retention: 4 days (345600 seconds)
  - Visibility timeout: 30 seconds
  - Receive wait time: 10 seconds (long polling)
  - Max receive count: 3 (before sending to DLQ)

- **Dead Letter Queue**: `{environment}-test-queue-dlq.fifo`
  - FIFO queue with content-based deduplication
  - Message retention: 14 days (1209600 seconds)

### RDS PostgreSQL Database

- **Instance**: `{environment}-async-dispatch-db`
  - Engine: PostgreSQL 17
  - Instance class: db.t3.micro (configurable)
  - Storage: 20GB with autoscaling up to 100GB
  - Encrypted storage (gp3)
  - Automated backups with 7-day retention
  - Performance Insights enabled
  - CloudWatch logs for PostgreSQL and upgrades
  - Security group with restricted access

## Configuration

### Using Environment Variables

Set the following environment variables:

```bash
# AWS Configuration
export TF_VAR_aws_region="us-east-1"
export TF_VAR_aws_access_key="your-access-key"
export TF_VAR_aws_secret_key="your-secret-key"
export TF_VAR_aws_account_id="123456789012"
export TF_VAR_environment="prod"

# RDS Configuration
export TF_VAR_vpc_id="vpc-xxxxxxxxxxxxxxxxx"
export TF_VAR_db_subnet_ids='["subnet-xxxxxxxxxxxxxxxxx", "subnet-yyyyyyyyyyyyyyyyy"]'
export TF_VAR_db_username="postgres"
export TF_VAR_db_password="your-secure-password"
```

### Using terraform.tfvars File

1. Copy the example file:
```bash
cp terraform.tfvars.example terraform.tfvars
```

2. Edit `terraform.tfvars` with your actual values:
```hcl
aws_region     = "us-east-1"
aws_access_key = "your-access-key"
aws_secret_key = "your-secret-key"
aws_account_id = "123456789012"
environment    = "prod"
```

**Note**: The `terraform.tfvars` file is gitignored for security.

## Usage

### Initialize Terraform

```bash
cd infrastructure/terraform
terraform init
```

### Plan Deployment

```bash
terraform plan
```

### Apply Configuration

```bash
terraform apply
```

### Destroy Resources

```bash
terraform destroy
```

## Outputs

After applying, Terraform will output:

### SQS Outputs
- `test_queue_url` - URL of the main queue
- `test_queue_arn` - ARN of the main queue
- `test_queue_dlq_url` - URL of the dead letter queue
- `test_queue_dlq_arn` - ARN of the dead letter queue
- `queue_name` - Name of the main queue

### RDS Outputs
- `rds_endpoint` - RDS instance endpoint (host:port)
- `rds_address` - RDS instance address (hostname only)
- `rds_port` - RDS instance port (5432)
- `rds_database_name` - Database name
- `rds_instance_id` - RDS instance identifier

## Security Best Practices

1. **Never commit** `terraform.tfvars` or any files containing credentials
2. Use IAM roles when running from EC2/ECS instead of access keys
3. Consider using AWS Secrets Manager or Parameter Store for credentials
4. Use Terraform Cloud or S3 backend for remote state storage
5. Enable MFA for AWS account access

## Integration with Application

Create a Spring Boot `application.properties` for production:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://${RDS_ENDPOINT}/async_dispatch
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# AWS Configuration
spring.cloud.aws.region.static=${AWS_REGION}
spring.cloud.aws.credentials.access-key=${AWS_ACCESS_KEY}
spring.cloud.aws.credentials.secret-key=${AWS_SECRET_KEY}
# Remove the endpoint override for production (use real AWS)
# spring.cloud.aws.sqs.endpoint=http://localhost:4566
```

Or use IAM roles (recommended for EC2/ECS):

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://${RDS_ENDPOINT}/async_dispatch
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# AWS Configuration
spring.cloud.aws.region.static=${AWS_REGION}
spring.cloud.aws.credentials.instance-profile=true
```

### Retrieving RDS Endpoint

After applying Terraform, get the RDS endpoint:

```bash
terraform output rds_endpoint
```
