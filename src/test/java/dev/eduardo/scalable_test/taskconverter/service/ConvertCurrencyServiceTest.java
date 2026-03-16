package dev.eduardo.scalable_test.taskconverter.service;

import dev.eduardo.scalable_test.common.sqs.ConvertCurrencyMessage;
import dev.eduardo.scalable_test.taskconverter.service.sqs.TaskResultProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConvertCurrencyService")
class ConvertCurrencyServiceTest {

    @Mock
    private TaskResultProducer taskResultProducer;

    private ConvertCurrencyService convertCurrencyService;

    @BeforeEach
    void setUp() {
        convertCurrencyService = new ConvertCurrencyService(taskResultProducer);
    }

    private ConvertCurrencyMessage message(UUID id, double amount, String from, String to) {
        return ConvertCurrencyMessage.builder()
                .id(id)
                .payload(ConvertCurrencyMessage.Payload.builder()
                        .amount(amount).fromCurrency(from).toCurrency(to).build())
                .build();
    }

    private String captureResult(UUID id) {
        var captor = ArgumentCaptor.forClass(String.class);
        verify(taskResultProducer).sendTaskResult(eq(id), captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("EUR to USD applies rate 1.10")
    void convertEurToUsd() {
        var id = UUID.randomUUID();
        convertCurrencyService.convertCurrency(message(id, 100.0, "EUR", "USD"));
        assertThat(captureResult(id)).isEqualTo("110.00");
    }

    @Test
    @DisplayName("USD to GBP applies rate 0.77")
    void convertUsdToGbp() {
        var id = UUID.randomUUID();
        convertCurrencyService.convertCurrency(message(id, 200.0, "USD", "GBP"));
        assertThat(captureResult(id)).isEqualTo("154.00");
    }

    @Test
    @DisplayName("GBP to USD applies rate 1.30")
    void convertGbpToUsd() {
        var id = UUID.randomUUID();
        convertCurrencyService.convertCurrency(message(id, 10.0, "GBP", "USD"));
        assertThat(captureResult(id)).isEqualTo("13.00");
    }

    @Test
    @DisplayName("Same currency returns unchanged amount")
    void convertSameCurrency() {
        var id = UUID.randomUUID();
        convertCurrencyService.convertCurrency(message(id, 50.0, "EUR", "EUR"));
        assertThat(captureResult(id)).isEqualTo("50.00");
    }

    @Test
    @DisplayName("Unknown source currency sends error result")
    void unknownFromCurrencySendsError() {
        var id = UUID.randomUUID();
        convertCurrencyService.convertCurrency(message(id, 100.0, "JPY", "USD"));
        assertThat(captureResult(id)).startsWith("Error:").contains("JPY");
    }

    @Test
    @DisplayName("Unknown target currency sends error result")
    void unknownToCurrencySendsError() {
        var id = UUID.randomUUID();
        convertCurrencyService.convertCurrency(message(id, 100.0, "EUR", "JPY"));
        assertThat(captureResult(id)).startsWith("Error:").contains("JPY");
    }
}
