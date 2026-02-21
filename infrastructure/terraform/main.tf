terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region     = var.aws_region
  access_key = var.aws_access_key
  secret_key = var.aws_secret_key
}

resource "aws_sqs_queue" "tasks_dlq" {
  name                        = "${var.environment}-tasks-dlq.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  message_retention_seconds   = 1209600

  tags = {
    Environment = var.environment
    Application = "scalable-test"
    ManagedBy   = "terraform"
  }
}

resource "aws_sqs_queue" "tasks" {
  name                        = "${var.environment}-tasks.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  delay_seconds               = 0
  max_message_size            = 262144
  message_retention_seconds   = 345600
  receive_wait_time_seconds   = 10
  visibility_timeout_seconds  = 30

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.tasks_dlq.arn
    maxReceiveCount     = 3
  })

  tags = {
    Environment = var.environment
    Application = "scalable-test"
    ManagedBy   = "terraform"
  }
}

resource "aws_sqs_queue" "task_results_dlq" {
  name                        = "${var.environment}-task-results-dlq.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  message_retention_seconds   = 1209600

  tags = {
    Environment = var.environment
    Application = "scalable-test"
    ManagedBy   = "terraform"
  }
}

resource "aws_sqs_queue" "task_results" {
  name                        = "${var.environment}-task-results.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  delay_seconds               = 0
  max_message_size            = 262144
  message_retention_seconds   = 345600
  receive_wait_time_seconds   = 10
  visibility_timeout_seconds  = 30

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.task_results_dlq.arn
    maxReceiveCount     = 3
  })

  tags = {
    Environment = var.environment
    Application = "scalable-test"
    ManagedBy   = "terraform"
  }
}

resource "aws_sqs_queue_policy" "tasks_policy" {
  queue_url = aws_sqs_queue.tasks.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowSendMessage"
        Effect = "Allow"
        Principal = {
          AWS = "*"
        }
        Action = [
          "sqs:SendMessage",
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ]
        Resource = aws_sqs_queue.tasks.arn
        Condition = {
          StringEquals = {
            "aws:SourceAccount" = var.aws_account_id
          }
        }
      }
    ]
  })
}

resource "aws_sqs_queue_policy" "task_results_policy" {
  queue_url = aws_sqs_queue.task_results.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowSendMessage"
        Effect = "Allow"
        Principal = {
          AWS = "*"
        }
        Action = [
          "sqs:SendMessage",
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ]
        Resource = aws_sqs_queue.task_results.arn
        Condition = {
          StringEquals = {
            "aws:SourceAccount" = var.aws_account_id
          }
        }
      }
    ]
  })
}
