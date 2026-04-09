package dev.eduardo.scalable_test.taskmanager.service;

import dev.eduardo.scalable_test.taskmanager.api.TaskRequest;
import dev.eduardo.scalable_test.taskmanager.api.TaskResults;
import dev.eduardo.scalable_test.taskmanager.domain.Task;
import dev.eduardo.scalable_test.taskmanager.domain.TaskPayload;
import dev.eduardo.scalable_test.common.sqs.CalculateInterestMessage;
import dev.eduardo.scalable_test.common.sqs.ConvertCurrencyMessage;
import dev.eduardo.scalable_test.taskmanager.service.sqs.TaskProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskManager {

    private final TaskService taskService;
    private final TaskProducer taskProducer;

    @Transactional(readOnly = true)
    public TaskResults getTask(UUID taskId) {
        var runningTask = taskService.findById(taskId);

        var results = (runningTask.getTaskResult() != null) ? runningTask.getTaskResult().getResult() : null;

        return TaskResults.builder()
                .id(runningTask.getId())
                .type(runningTask.getType())
                .payload(runningTask.getPayload())
                .result(results)
                .status(runningTask.getStatus())
                .build();
    }

    @Transactional
    public Task enqueueTask(TaskRequest taskRequest) {

        try {
            var createdTask = taskService.save(Task.builder()
                    .type(taskRequest.type())
                    .payload(taskRequest.payload())
                    .status(Task.TaskStatus.PENDING)
                    .build());

            switch (taskRequest.type()) {
                case CONVERT_CURRENCY:
                    enqueueConvertCurrencyTask(createdTask.getId(), taskRequest.payload());
                    break;
                case CALCULATE_INTEREST:
                    enqueueCalculateInterestTask(createdTask.getId(), taskRequest.payload());
                    break;
                default:
                    throw new IllegalArgumentException("Task type not supported");
            }

            return createdTask;
        } catch (RuntimeException e) {
            log.error("Error enqueueing task", e);
            throw e;
        }
    }

    private void enqueueConvertCurrencyTask(UUID taskId, TaskPayload taskPayload) {
        var payload = ConvertCurrencyMessage.Payload.builder()
                .toCurrency(taskPayload.toCurrency().name())
                .fromCurrency(taskPayload.fromCurrency().name())
                .amount(taskPayload.amount())
                .build();

        taskProducer.enqueueConvertCurrencyTask(taskId, payload);
    }

    private void enqueueCalculateInterestTask(UUID taskId, TaskPayload taskPayload) {
        var payload = CalculateInterestMessage.Payload.builder()
                .days(taskPayload.days())
                .annualRate(taskPayload.annualRate())
                .principal(taskPayload.principal())
                .build();

        taskProducer.enqueueCalculateInterestTask(taskId, payload);
    }
}
