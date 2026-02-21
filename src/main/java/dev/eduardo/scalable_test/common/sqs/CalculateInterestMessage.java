package dev.eduardo.scalable_test.common.sqs;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.eduardo.scalable_test.common.domain.TaskType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Builder
@Value
public class CalculateInterestMessage {

    @NotNull
    @JsonProperty
    UUID taskId;

    @JsonProperty
    TaskType type = TaskType.CALCULATE_INTEREST;

    @NotNull
    @JsonProperty
    Payload payload;

    @Builder
    public record Payload(@NotNull @Min(0) Double principal,
                          @NotNull @Min(0) Double annualRate,
                          @Min(1) int days) {

    }
}
