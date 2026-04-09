package dev.eduardo.scalable_test.common.sqs;

import java.util.UUID;

public record TaskResultMessage(UUID taskId, String result) {
}
