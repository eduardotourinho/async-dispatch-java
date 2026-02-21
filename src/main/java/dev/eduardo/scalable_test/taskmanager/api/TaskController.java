package dev.eduardo.scalable_test.taskmanager.api;


import dev.eduardo.scalable_test.taskmanager.service.TaskManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskManager taskManager;


    @GetMapping("/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    public TaskResults getTask(@PathVariable("taskId") UUID taskId) {
        return  taskManager.getTask(taskId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse enqueueTask(@RequestBody TaskRequest taskRequest) {
        var createdTask = taskManager.enqueueTask(taskRequest);

        return new TaskResponse(createdTask.getId());
    }
}
