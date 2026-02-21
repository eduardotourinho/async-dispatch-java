package dev.eduardo.scalable_test.taskmanager.api;

import dev.eduardo.scalable_test.taskmanager.domain.TaskPayload;
import dev.eduardo.scalable_test.common.domain.TaskType;

public record TaskRequest(
        TaskType type,
        TaskPayload payload
) {
}
