# LocalStack SQS Configuration

This project is configured to use AWS LocalStack for local SQS development with FIFO queues.

## Prerequisites

- Docker and Docker Compose installed
- AWS CLI installed (for testing)

## Starting LocalStack

```bash
docker-compose up sqs -d
```

This will start:
- **LocalStack SQS** on port `4566`

The initialization script (`infrastructure/localstack/init-sqs.sh`) automatically creates all required queues.

## Queues Created

The initialization script creates the following FIFO queues with dead letter queue configuration:

### Main Queues
- `tasks.fifo` - Task processing queue
  - Dead Letter Queue: `tasks-dlq.fifo`
  - Max Receive Count: 3
  - Content-Based Deduplication: Enabled

- `task-results.fifo` - Task results queue
  - Dead Letter Queue: `task-results-dlq.fifo`
  - Max Receive Count: 3
  - Content-Based Deduplication: Enabled

### Dead Letter Queues
- `tasks-dlq.fifo` - Failed task messages
- `task-results-dlq.fifo` - Failed result messages

## Configuration

The application is configured to connect to LocalStack via:
- **Endpoint**: `http://localhost:4566`
- **Region**: `us-east-1`
- **Credentials**: test/test (dummy credentials for local development)
- **Tasks Queue URL**: `http://localhost:4566/000000000000/tasks.fifo`
- **Task Results Queue URL**: `http://localhost:4566/000000000000/task-results.fifo`


## Troubleshooting

### Queues Not Created

If queues are not created automatically, restart LocalStack:

```bash
docker-compose restart sqs
```

Check the initialization logs:

```bash
docker logs localstack-sqs
```

### Reinitialize Queues

If you need to recreate the queues:

```bash
# Stop LocalStack
docker-compose down

# Remove LocalStack data
rm -rf .localstack

# Start LocalStack again
docker-compose up -d
```

### Verify Queue Creation

```bash
# List all queues
aws --endpoint-url=http://localhost:4566 sqs list-queues

# Expected output should show 4 queues:
# - tasks.fifo
# - tasks-dlq.fifo
# - task-results.fifo
# - task-results-dlq.fifo
```

## Stopping LocalStack

```bash
# Stop containers
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

See the main [README.md](README.md) for complete application setup and usage instructions.
