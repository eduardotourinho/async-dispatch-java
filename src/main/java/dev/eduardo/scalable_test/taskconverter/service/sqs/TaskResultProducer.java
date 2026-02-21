package dev.eduardo.scalable_test.taskconverter.service.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskResultProducer {

    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;

    @Value("${spring.cloud.aws.sqs.task-results-queue-url}")
    private String queueUrl;

    public void sendTaskResult(UUID taskId, String result) {
        try {
            var message = new TaskResultMessage(taskId, result);

            var messageBody = objectMapper.writeValueAsString(message);
            
            sqsAsyncClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .messageGroupId("task-results-group")
                    .build());
            
            log.info("Sent task result for task: {}", taskId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize task result", e);
            throw new RuntimeException("Failed to send task result", e);
        }
    }
}
