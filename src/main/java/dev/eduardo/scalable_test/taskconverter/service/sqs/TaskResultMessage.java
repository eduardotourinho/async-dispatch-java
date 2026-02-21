package dev.eduardo.scalable_test.taskconverter.service.sqs;

import java.util.UUID;

public record TaskResultMessage(UUID taskId, String result) {
}
