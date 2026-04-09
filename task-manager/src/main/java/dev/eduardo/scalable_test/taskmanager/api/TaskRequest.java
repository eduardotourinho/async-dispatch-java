package dev.eduardo.scalable_test.taskmanager.api;

import dev.eduardo.scalable_test.taskmanager.domain.TaskPayload;
import dev.eduardo.scalable_test.common.domain.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to enqueue a new task")
public record TaskRequest(
        @NotNull(message = "Task type must not be null")
        @Schema(description = "The type of task to execute", example = "CONVERT_CURRENCY", requiredMode = Schema.RequiredMode.REQUIRED)
        TaskType type,

        @NotNull(message = "Task payload must not be null")
        @Schema(description = "Input data for the task", requiredMode = Schema.RequiredMode.REQUIRED)
        TaskPayload payload
) {
}
