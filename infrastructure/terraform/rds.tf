resource "aws_db_subnet_group" "main" {
  name       = "${var.environment}-scalable-test-db-subnet-group"
  subnet_ids = var.db_subnet_ids

  tags = {
    Name        = "${var.environment}-scalable-test-db-subnet-group"
    Environment = var.environment
    Application = "scalable-test"
    ManagedBy   = "terraform"
  }
}

resource "aws_security_group" "rds" {
  name        = "${var.environment}-scalable-test-rds-sg"
  description = "Security group for RDS PostgreSQL instance"
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_blocks
    description = "PostgreSQL access"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name        = "${var.environment}-scalable-test-rds-sg"
    Environment = var.environment
    Application = "scalable-test"
    ManagedBy   = "terraform"
  }
}

resource "aws_db_instance" "postgres" {
  identifier     = "${var.environment}-scalable-test-db"
  engine         = "postgres"
  engine_version = var.postgres_version
  instance_class = var.db_instance_class

  allocated_storage     = var.db_allocated_storage
  max_allocated_storage = var.db_max_allocated_storage
  storage_type          = "gp3"
  storage_encrypted     = true

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password
  port     = 5432

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = var.db_publicly_accessible

  backup_retention_period = var.db_backup_retention_period
  backup_window           = "03:00-04:00"
  maintenance_window      = "mon:04:00-mon:05:00"

  enabled_cloudwatch_logs_exports = ["postgresql", "upgrade"]

  skip_final_snapshot       = var.skip_final_snapshot
  final_snapshot_identifier = var.skip_final_snapshot ? null : "${var.environment}-scalable-test-db-final-snapshot-${formatdate("YYYY-MM-DD-hhmm", timestamp())}"

  deletion_protection = var.deletion_protection

  performance_insights_enabled          = true
  performance_insights_retention_period = 7

  tags = {
    Name        = "${var.environment}-scalable-test-db"
    Environment = var.environment
    Application = "scalable-test"
    ManagedBy   = "terraform"
  }
}
