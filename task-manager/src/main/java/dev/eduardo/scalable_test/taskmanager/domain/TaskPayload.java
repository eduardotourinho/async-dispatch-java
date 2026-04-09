package dev.eduardo.scalable_test.taskmanager.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import dev.eduardo.scalable_test.taskmanager.api.SupportedCurrency;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskPayload(
        Double amount,
        SupportedCurrency fromCurrency,
        SupportedCurrency toCurrency,
        Double principal,
        Double annualRate,
        Integer days
) {
}
