# Tasks Queue Outputs
output "tasks_queue_url" {
  description = "URL of the tasks queue"
  value       = aws_sqs_queue.tasks.url
}

output "tasks_queue_arn" {
  description = "ARN of the tasks queue"
  value       = aws_sqs_queue.tasks.arn
}

output "tasks_queue_name" {
  description = "Name of the tasks queue"
  value       = aws_sqs_queue.tasks.name
}

output "tasks_dlq_url" {
  description = "URL of the tasks dead letter queue"
  value       = aws_sqs_queue.tasks_dlq.url
}

output "tasks_dlq_arn" {
  description = "ARN of the tasks dead letter queue"
  value       = aws_sqs_queue.tasks_dlq.arn
}

# Task Results Queue Outputs
output "task_results_queue_url" {
  description = "URL of the task results queue"
  value       = aws_sqs_queue.task_results.url
}

output "task_results_queue_arn" {
  description = "ARN of the task results queue"
  value       = aws_sqs_queue.task_results.arn
}

output "task_results_queue_name" {
  description = "Name of the task results queue"
  value       = aws_sqs_queue.task_results.name
}

output "task_results_dlq_url" {
  description = "URL of the task results dead letter queue"
  value       = aws_sqs_queue.task_results_dlq.url
}

output "task_results_dlq_arn" {
  description = "ARN of the task results dead letter queue"
  value       = aws_sqs_queue.task_results_dlq.arn
}

# RDS Outputs
output "rds_endpoint" {
  description = "RDS instance endpoint"
  value       = aws_db_instance.postgres.endpoint
}

output "rds_address" {
  description = "RDS instance address"
  value       = aws_db_instance.postgres.address
}

output "rds_port" {
  description = "RDS instance port"
  value       = aws_db_instance.postgres.port
}

output "rds_database_name" {
  description = "RDS database name"
  value       = aws_db_instance.postgres.db_name
}

output "rds_instance_id" {
  description = "RDS instance identifier"
  value       = aws_db_instance.postgres.id
}
