package dev.eduardo.scalable_test.taskmanager.api;

import dev.eduardo.scalable_test.taskmanager.service.TaskManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task submission and status retrieval")
public class TaskController {

    private final TaskManager taskManager;

    @Operation(summary = "Get task by ID", description = "Returns the current status and result of the specified task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    public TaskResults getTask(@PathVariable("taskId") UUID taskId) {
        return taskManager.getTask(taskId);
    }

    @Operation(summary = "Enqueue a new task", description = "Submits a new task for async processing via SQS")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task successfully enqueued"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse enqueueTask(@Valid @RequestBody TaskRequest taskRequest) {
        var createdTask = taskManager.enqueueTask(taskRequest);
        return new TaskResponse(createdTask.getId());
    }
}
