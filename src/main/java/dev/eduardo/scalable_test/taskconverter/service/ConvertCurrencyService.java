package dev.eduardo.scalable_test.taskconverter.service;

import dev.eduardo.scalable_test.taskconverter.service.sqs.TaskResultProducer;
import dev.eduardo.scalable_test.common.sqs.ConvertCurrencyMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConvertCurrencyService {

    private final TaskResultProducer taskResultProducer;

    // Mock exchange rates (in production, this would come from an external API)
    private static final Map<String, Map<String, BigDecimal>> EXCHANGE_RATES = Map.of(
            "EUR", Map.of(
                    "USD", new BigDecimal("1.10"),
                    "GBP", new BigDecimal("0.85"),
                    "EUR", BigDecimal.ONE
            ),
            "USD", Map.of(
                    "EUR", new BigDecimal("0.91"),
                    "GBP", new BigDecimal("0.77"),
                    "USD", BigDecimal.ONE
            ),
            "GBP", Map.of(
                    "EUR", new BigDecimal("1.18"),
                    "USD", new BigDecimal("1.30"),
                    "GBP", BigDecimal.ONE
            )
    );

    public void convertCurrency(ConvertCurrencyMessage taskMessage) {
        try {
            var payload = taskMessage.getPayload();
            log.info("Converting {} {} to {}", payload.amount(), payload.fromCurrency(), payload.toCurrency());

            var amount = BigDecimal.valueOf(payload.amount());
            var rate = getExchangeRate(payload.fromCurrency(), payload.toCurrency());
            var convertedAmount = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);

            var result = convertedAmount.toString();

            log.info("Conversion result: {}", result);
            taskResultProducer.sendTaskResult(taskMessage.getId(), result);

        } catch (Exception e) {
            log.error("Error converting currency for task {}", taskMessage.getId(), e);
            taskResultProducer.sendTaskResult(taskMessage.getId(), "Error: " + e.getMessage());
        }
    }

    private BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        Map<String, BigDecimal> rates = EXCHANGE_RATES.get(fromCurrency);
        if (rates == null) {
            throw new IllegalArgumentException("Unsupported currency: " + fromCurrency);
        }
        BigDecimal rate = rates.get(toCurrency);
        if (rate == null) {
            throw new IllegalArgumentException("Unsupported currency pair: " + fromCurrency + " to " + toCurrency);
        }
        return rate;
    }
}
