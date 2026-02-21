package dev.eduardo.scalable_test.taskconverter.service;

import dev.eduardo.scalable_test.taskconverter.service.sqs.TaskResultProducer;
import dev.eduardo.scalable_test.common.sqs.CalculateInterestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculateInterestService {

    private final TaskResultProducer taskResultProducer;

    public void calculateInterest(CalculateInterestMessage taskMessage) {
        try {
            var payload = taskMessage.getPayload();
            log.info("Calculating interest for principal: {}, rate: {}, days: {}",
                    payload.principal(), payload.annualRate(), payload.days());

            // Simple interest formula: I = P * R * T
            // Where: P = principal, R = annual rate (as decimal), T = time in years
            var principal = BigDecimal.valueOf(payload.principal());
            var annualRate = BigDecimal.valueOf(payload.annualRate()).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
            var timeInYears = BigDecimal.valueOf(payload.days()).divide(BigDecimal.valueOf(365), 6, RoundingMode.HALF_UP);

            var interest = principal
                    .multiply(annualRate)
                    .multiply(timeInYears)
                    .setScale(2, RoundingMode.HALF_UP);

            var totalAmount = principal.add(interest).setScale(2, RoundingMode.HALF_UP);

            var result = totalAmount.toString();

            log.info("Interest calculation result: {}", result);
            taskResultProducer.sendTaskResult(taskMessage.getTaskId(), result);

        } catch (Exception e) {
            log.error("Error calculating interest for task {}", taskMessage.getTaskId(), e);
            taskResultProducer.sendTaskResult(taskMessage.getTaskId(), "Error: " + e.getMessage());
        }
    }
}
