package dev.eduardo.scalable_test.taskmanager.service.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.eduardo.scalable_test.taskconverter.service.sqs.TaskResultMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskResultsDlqListener {

    private final ObjectMapper objectMapper;

    @SqsListener(value = "${spring.cloud.aws.sqs.task-results-dlq-queue-url}", id = "task-results-dlq-listener")
    public void handleDeadLetteredTaskResult(String message) {
        log.error("Dead-lettered task result message received (exceeded max retries): {}", message);

        try {
            TaskResultMessage resultMessage = objectMapper.readValue(message, TaskResultMessage.class);
            log.error("Dead-lettered result for task ID: {}, result: {}", resultMessage.taskId(), resultMessage.result());
        } catch (Exception e) {
            log.error("Failed to deserialize dead-lettered task result message body: {}", message, e);
        }
    }
}
