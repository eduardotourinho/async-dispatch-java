package dev.eduardo.scalable_test.taskmanager.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.eduardo.scalable_test.common.domain.TaskType;
import dev.eduardo.scalable_test.taskmanager.domain.Task;
import dev.eduardo.scalable_test.taskmanager.domain.TaskPayload;
import dev.eduardo.scalable_test.taskmanager.service.TaskManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(dev.eduardo.scalable_test.common.api.GlobalExceptionHandler.class)
@DisplayName("TaskController")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskManager taskManager;

    @Test
    @DisplayName("POST /api/tasks returns 201 with taskId for a valid request")
    void enqueueTaskReturns201() throws Exception {
        var taskId = UUID.randomUUID();
        var payload = new TaskPayload(100.0, SupportedCurrency.EUR, SupportedCurrency.USD, null, null, null);
        var task = Task.builder()
                .id(taskId)
                .type(TaskType.CONVERT_CURRENCY)
                .payload(payload)
                .status(Task.TaskStatus.PENDING)
                .build();

        when(taskManager.enqueueTask(any())).thenReturn(task);

        var request = new TaskRequest(TaskType.CONVERT_CURRENCY, payload);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").value(taskId.toString()));
    }

    @Test
    @DisplayName("POST /api/tasks returns 400 when type is null")
    void enqueueTaskReturns400WhenTypeIsNull() throws Exception {
        var payload = new TaskPayload(100.0, SupportedCurrency.EUR, SupportedCurrency.USD, null, null, null);
        var request = new TaskRequest(null, payload);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/tasks returns 400 when payload is null")
    void enqueueTaskReturns400WhenPayloadIsNull() throws Exception {
        var request = new TaskRequest(TaskType.CONVERT_CURRENCY, null);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/tasks/{taskId} returns 200 with task details")
    void getTaskReturns200() throws Exception {
        var taskId = UUID.randomUUID();
        var payload = new TaskPayload(100.0, SupportedCurrency.EUR, SupportedCurrency.USD, null, null, null);
        var results = TaskResults.builder()
                .id(taskId)
                .type(TaskType.CONVERT_CURRENCY)
                .payload(payload)
                .status(Task.TaskStatus.PENDING)
                .build();

        when(taskManager.getTask(eq(taskId))).thenReturn(results);

        mockMvc.perform(get("/api/tasks/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/tasks/{taskId} returns 404 when task does not exist")
    void getTaskReturns404WhenNotFound() throws Exception {
        var taskId = UUID.randomUUID();
        when(taskManager.getTask(eq(taskId)))
                .thenThrow(new NoSuchElementException("Task not found with id: " + taskId));

        mockMvc.perform(get("/api/tasks/" + taskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
