package dev.eduardo.scalable_test.taskmanager.service.sqs;

import dev.eduardo.scalable_test.taskconverter.service.sqs.TaskResultMessage;
import dev.eduardo.scalable_test.taskmanager.domain.Task;
import dev.eduardo.scalable_test.taskmanager.domain.TaskResult;
import dev.eduardo.scalable_test.taskmanager.service.TaskService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskResultsSqsListener {

    private final TaskService taskService;

    @SqsListener("${spring.cloud.aws.sqs.task-results-queue-url}")
    @Transactional
    public void receiveTaskResult(TaskResultMessage resultMessage) {
        log.info("Received task result for task: {}", resultMessage.taskId());
        
        Task task = taskService.findById(resultMessage.taskId());
        if (task == null) {
            log.error("Task not found: {}", resultMessage.taskId());
            return;
        }
        
        // Create and save task result
        var taskResult = TaskResult.builder()
                .task(task)
                .result(resultMessage.result())
                .build();
        
        // Update task status and link result
        task.setStatus(Task.TaskStatus.COMPLETED);
        task.setTaskResult(taskResult);
        
        taskService.save(task);
        
        log.info("Task {} completed with result: {}", task.getId(), resultMessage.result());
    }
}
