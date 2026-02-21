package dev.eduardo.scalable_test.common.sqs;


import com.fasterxml.jackson.annotation.JsonProperty;
import dev.eduardo.scalable_test.common.domain.TaskType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.UUID;

@Builder
@RequiredArgsConstructor
@Value
public class ConvertCurrencyMessage {

    @NotNull
    @JsonProperty
    UUID id;

    @JsonProperty
    TaskType type = TaskType.CONVERT_CURRENCY;

    @NotNull
    @JsonProperty
    Payload payload;

    @Builder
    public record Payload(@Min(0) Double amount,
                          @NotNull @NotBlank String fromCurrency,
                          @NotNull @NotBlank String toCurrency) {

    }
}
