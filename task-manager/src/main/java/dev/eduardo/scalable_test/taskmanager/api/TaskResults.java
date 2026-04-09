package dev.eduardo.scalable_test.taskmanager.api;

import dev.eduardo.scalable_test.taskmanager.domain.Task;
import dev.eduardo.scalable_test.taskmanager.domain.TaskPayload;
import dev.eduardo.scalable_test.common.domain.TaskType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record TaskResults(UUID id, TaskType type, TaskPayload payload, String result, Task.TaskStatus status) {
}
