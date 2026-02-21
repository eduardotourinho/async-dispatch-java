package dev.eduardo.scalable_test.taskmanager.service.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.eduardo.scalable_test.common.sqs.CalculateInterestMessage;
import dev.eduardo.scalable_test.common.sqs.ConvertCurrencyMessage;
import dev.eduardo.scalable_test.common.domain.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskProducer {

    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;

    @Value("${spring.cloud.aws.sqs.tasks-queue-url}")
    private String queueUrl;

    public void enqueueConvertCurrencyTask(UUID taskId, @Validated ConvertCurrencyMessage.Payload payload) {
        var message = ConvertCurrencyMessage.builder()
                .id(taskId)
                .payload(payload)
                .build();

        enqueueTask(message.getType(), message);
    }

    public void enqueueCalculateInterestTask(UUID taskId, @Validated CalculateInterestMessage.Payload payload) {
        var message = CalculateInterestMessage.builder()
                .taskId(taskId)
                .payload(payload)
                .build();

        enqueueTask(message.getType(), message);
    }

    private void enqueueTask(TaskType taskType, Object message) {
        try {
            var messageBody = objectMapper.writeValueAsString(message);

            sqsAsyncClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .messageGroupId(taskType.name())
                    .build());
            log.info("Enqueued calculate interest task: {}", messageBody);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message", e);
            throw new RuntimeException("Failed to enqueue message", e);
        }
    }
}
