package dev.eduardo.scalable_test.common.domain;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum TaskType {

    CONVERT_CURRENCY("convert_currency"),
    CALCULATE_INTEREST("calculate_interest");

    @JsonValue
    private final String name;

    @JsonCreator
    public static TaskType fromValue(String value) {
        try {
            return TaskType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid task type value: {}", value);
            throw e;
        }
    }
}
