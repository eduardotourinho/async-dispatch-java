#!/bin/bash

echo "Initializing SQS FIFO queues in LocalStack..."

# Create Dead Letter Queues first
TASKS_DLQ_URL=$(awslocal sqs create-queue \
  --queue-name tasks-dlq.fifo \
  --attributes FifoQueue=true,ContentBasedDeduplication=true \
  --query 'QueueUrl' \
  --output text)

TASK_RESULTS_DLQ_URL=$(awslocal sqs create-queue \
  --queue-name task-results-dlq.fifo \
  --attributes FifoQueue=true,ContentBasedDeduplication=true \
  --query 'QueueUrl' \
  --output text)

# Get DLQ ARNs using the URLs returned from creation
TASKS_DLQ_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "${TASKS_DLQ_URL}" \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' \
  --output text)

TASK_RESULTS_DLQ_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "${TASK_RESULTS_DLQ_URL}" \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' \
  --output text)

# Create main queues with redrive policies
awslocal sqs create-queue \
  --queue-name tasks.fifo \
  --attributes '{"FifoQueue":"true","ContentBasedDeduplication":"true","RedrivePolicy":"{\"deadLetterTargetArn\":\"'"${TASKS_DLQ_ARN}"'\",\"maxReceiveCount\":\"3\"}"}'

awslocal sqs create-queue \
  --queue-name task-results.fifo \
  --attributes '{"FifoQueue":"true","ContentBasedDeduplication":"true","RedrivePolicy":"{\"deadLetterTargetArn\":\"'"${TASK_RESULTS_DLQ_ARN}"'\",\"maxReceiveCount\":\"3\"}"}'

echo "SQS FIFO queues created successfully!"
