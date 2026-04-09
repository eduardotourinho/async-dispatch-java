-- Flyway migration V1: initial schema
-- Replaces hibernate.ddl-auto=update with versioned, auditable schema management.
-- Note: task_results is created first to satisfy the FK from tasks.

CREATE TABLE IF NOT EXISTS task_results (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    result     TEXT,
    task_id    UUID,                           -- back-reference; FK added after tasks table
    created_at TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS tasks (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    type           VARCHAR(50) NOT NULL,
    payload        JSONB       NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP   NOT NULL DEFAULT now(),
    task_result_id UUID        REFERENCES task_results(id)
);

ALTER TABLE task_results
    ADD CONSTRAINT fk_task_results_task
    FOREIGN KEY (task_id) REFERENCES tasks(id);

CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_type   ON tasks(type);
