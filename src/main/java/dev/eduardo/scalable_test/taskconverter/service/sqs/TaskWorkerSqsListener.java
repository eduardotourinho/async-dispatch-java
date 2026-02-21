package dev.eduardo.scalable_test.taskconverter.service.sqs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.eduardo.scalable_test.common.domain.TaskType;
import dev.eduardo.scalable_test.taskconverter.service.CalculateInterestService;
import dev.eduardo.scalable_test.taskconverter.service.ConvertCurrencyService;
import dev.eduardo.scalable_test.common.sqs.CalculateInterestMessage;
import dev.eduardo.scalable_test.common.sqs.ConvertCurrencyMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskWorkerSqsListener {

    private final CalculateInterestService calculateInterestService;
    private final ConvertCurrencyService convertCurrencyService;
    private final ObjectMapper objectMapper;

    @SqsListener(value = "${spring.cloud.aws.sqs.tasks-queue-url}", id = "task-worker-listener")
    public void handleTaskMessage(String message) {
        try {
            var jsonNode = objectMapper.readTree(message);
            var typeStr = jsonNode.get("type").asText();
            var taskType = TaskType.fromValue(typeStr);
            
            switch (taskType) {
                case CONVERT_CURRENCY:
                    handleConvertCurrencyTask(objectMapper.readValue(message, ConvertCurrencyMessage.class));
                    break;
                case CALCULATE_INTEREST:
                    handleCalculateInterestTask(objectMapper.readValue(message, CalculateInterestMessage.class));
                    break;
                default:
                    log.error("Unknown task type: {}", taskType);
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
            throw new RuntimeException("Failed to process message", e);
        }
    }
    
    private void handleConvertCurrencyTask(ConvertCurrencyMessage taskMessage) {
        log.info("Processing convert currency task: {} with payload: {}", taskMessage.getId(), taskMessage.getPayload());
        convertCurrencyService.convertCurrency(taskMessage);
    }
    
    private void handleCalculateInterestTask(CalculateInterestMessage taskMessage) {
        log.info("Processing calculate interest task: {} with payload: {}", taskMessage.getTaskId(), taskMessage.getPayload());
        calculateInterestService.calculateInterest(taskMessage);
    }
}
