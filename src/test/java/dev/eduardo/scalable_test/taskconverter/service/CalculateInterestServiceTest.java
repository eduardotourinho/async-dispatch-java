package dev.eduardo.scalable_test.taskconverter.service;

import dev.eduardo.scalable_test.common.sqs.CalculateInterestMessage;
import dev.eduardo.scalable_test.taskconverter.service.sqs.TaskResultProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalculateInterestService")
class CalculateInterestServiceTest {

    @Mock
    private TaskResultProducer taskResultProducer;

    private CalculateInterestService calculateInterestService;

    @BeforeEach
    void setUp() {
        calculateInterestService = new CalculateInterestService(taskResultProducer);
    }

    private CalculateInterestMessage message(UUID id, double principal, double rate, int days) {
        return CalculateInterestMessage.builder()
                .taskId(id)
                .payload(CalculateInterestMessage.Payload.builder()
                        .principal(principal).annualRate(rate).days(days).build())
                .build();
    }

    private String captureResult(UUID id) {
        var captor = ArgumentCaptor.forClass(String.class);
        verify(taskResultProducer).sendTaskResult(eq(id), captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("Standard: P=1000, R=10%, 365 days → total 1100.00")
    void standardInterestFullYear() {
        var id = UUID.randomUUID();
        calculateInterestService.calculateInterest(message(id, 1000.0, 10.0, 365));
        assertThat(new BigDecimal(captureResult(id))).isEqualByComparingTo("1100.00");
    }

    @Test
    @DisplayName("Zero interest rate → total equals principal")
    void zeroRateReturnsPrincipal() {
        var id = UUID.randomUUID();
        calculateInterestService.calculateInterest(message(id, 500.0, 0.0, 180));
        assertThat(new BigDecimal(captureResult(id))).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Zero principal → total is 0.00")
    void zeroPrincipalReturnsZero() {
        var id = UUID.randomUUID();
        calculateInterestService.calculateInterest(message(id, 0.0, 5.0, 365));
        assertThat(new BigDecimal(captureResult(id))).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Half year (182 days) at 10% → total between 1049 and 1051")
    void halfYearInterest() {
        var id = UUID.randomUUID();
        calculateInterestService.calculateInterest(message(id, 1000.0, 10.0, 182));
        // I = 1000 * 0.10 * (182/365) ≈ 49.86 → total ≈ 1049.86
        var total = new BigDecimal(captureResult(id));
        assertThat(total).isGreaterThan(new BigDecimal("1049.00"))
                .isLessThan(new BigDecimal("1051.00"));
    }

    @Test
    @DisplayName("Result is always rounded to exactly 2 decimal places")
    void resultHasTwoDecimalPlaces() {
        var id = UUID.randomUUID();
        calculateInterestService.calculateInterest(message(id, 100.0, 7.0, 1));
        assertThat(captureResult(id)).matches("\\d+\\.\\d{2}");
    }
}
