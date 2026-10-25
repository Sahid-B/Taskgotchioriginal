package ec.edu.espe.pa.taskgotchi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.espe.pa.taskgotchi.config.SecurityConfig;
import ec.edu.espe.pa.taskgotchi.dto.PageResponse;
import ec.edu.espe.pa.taskgotchi.dto.TaskRequest;
import ec.edu.espe.pa.taskgotchi.dto.TaskResponse;
import ec.edu.espe.pa.taskgotchi.exception.DuplicateResourceException;
import ec.edu.espe.pa.taskgotchi.model.TaskDifficulty;
import ec.edu.espe.pa.taskgotchi.model.TaskPriority;
import ec.edu.espe.pa.taskgotchi.model.TaskStatus;
import ec.edu.espe.pa.taskgotchi.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    void getTasks_Returns200AndPageResponse() throws Exception {
        when(taskService.getTasks(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(PageResponse.from(new PageImpl<>(List.of())));

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page.number").value(0));
    }

    @Test
    void searchTasksByTitle_MissingTitle_Returns400() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/buscar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Falta el parámetro de consulta obligatorio: title"));
    }

    @Test
    void createTask_ValidRequest_Returns201Created() throws Exception {
        TaskRequest request = new TaskRequest("TSK-01", "Nueva Tarea", "Descripción", new BigDecimal("2.0"), true, null, TaskPriority.HIGH, TaskDifficulty.MEDIUM, null, null, null);
        TaskResponse response = new TaskResponse(1L, "TSK-01", "Nueva Tarea", "Descripción", new BigDecimal("2.0"), true, null, TaskPriority.HIGH, TaskDifficulty.MEDIUM, null, TaskStatus.PENDING, 50, 25, null, LocalDateTime.now(), LocalDateTime.now(), null, null);

        when(taskService.createTask(any(TaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.code").value("TSK-01"))
                .andExpect(jsonPath("$.title").value("Nueva Tarea"));
    }

    @Test
    void createTask_DuplicateCode_Returns409Conflict() throws Exception {
        TaskRequest request = new TaskRequest("TSK-01", "Nueva Tarea", "Descripción", new BigDecimal("2.0"), true, null, TaskPriority.HIGH, TaskDifficulty.MEDIUM, null, null, null);

        when(taskService.createTask(any(TaskRequest.class)))
                .thenThrow(new DuplicateResourceException("Ya existe una tarea registrada con el código: TSK-01"));

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Ya existe una tarea registrada con el código: TSK-01"));
    }

    @Test
    void createTask_InvalidRequest_Returns400BadRequestWithErrorsList() throws Exception {
        TaskRequest invalidRequest = new TaskRequest("", "", "Descripción", new BigDecimal("-1.0"), true, null, null, null, null, null, null);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void completeTask_Returns200Ok() throws Exception {
        TaskResponse response = new TaskResponse(1L, "TSK-01", "Nueva Tarea", "Descripción", new BigDecimal("2.0"), true, null, TaskPriority.HIGH, TaskDifficulty.MEDIUM, null, TaskStatus.COMPLETED, 50, 25, null, LocalDateTime.now(), LocalDateTime.now(), null, null);
        when(taskService.completeTask(eq(1L))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tasks/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
