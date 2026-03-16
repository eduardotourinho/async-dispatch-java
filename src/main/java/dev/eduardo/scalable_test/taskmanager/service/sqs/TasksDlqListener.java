package dev.eduardo.scalable_test.taskmanager.service.sqs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.eduardo.scalable_test.taskmanager.domain.Task;
import dev.eduardo.scalable_test.taskmanager.service.TaskService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TasksDlqListener {

    private final TaskService taskService;
    private final ObjectMapper objectMapper;

    @SqsListener(value = "${spring.cloud.aws.sqs.tasks-dlq-queue-url}", id = "tasks-dlq-listener")
    @Transactional
    public void handleDeadLetteredTask(String message) {
        log.error("Dead-lettered task message received (exceeded max retries): {}", message);

        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            // ConvertCurrencyMessage uses "id"; CalculateInterestMessage uses "taskId"
            UUID taskId = null;
            if (jsonNode.hasNonNull("taskId")) {
                taskId = UUID.fromString(jsonNode.get("taskId").asText());
            } else if (jsonNode.hasNonNull("id")) {
                taskId = UUID.fromString(jsonNode.get("id").asText());
            }

            if (taskId == null) {
                log.error("Could not extract task ID from dead-lettered message body: {}", message);
                return;
            }

            Task task = taskService.findById(taskId);
            task.setStatus(Task.TaskStatus.CANCELLED);
            taskService.save(task);
            log.error("Task {} marked as CANCELLED after exceeding max retries", taskId);

        } catch (NoSuchElementException e) {
            log.error("Dead-lettered task message references unknown task ID: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to process dead-lettered task message body: {}", message, e);
        }
    }
}
