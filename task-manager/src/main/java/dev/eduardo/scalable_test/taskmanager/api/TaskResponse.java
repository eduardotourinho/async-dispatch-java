package dev.eduardo.scalable_test.taskmanager.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response returned after successfully enqueuing a task")
public record TaskResponse(
        @Schema(description = "The unique identifier of the created task")
        UUID taskId
) {
}
