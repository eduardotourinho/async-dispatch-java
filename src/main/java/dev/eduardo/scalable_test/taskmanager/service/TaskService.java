package dev.eduardo.scalable_test.taskmanager.service;

import dev.eduardo.scalable_test.taskmanager.domain.Task;
import dev.eduardo.scalable_test.taskmanager.domain.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;


    @Transactional(readOnly = true)
    public Task findById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + id));
    }

    @Transactional
    public Task save(Task task) {
        return taskRepository.save(task);
    }
}
