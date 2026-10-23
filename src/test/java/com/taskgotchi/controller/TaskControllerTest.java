package com.taskgotchi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskgotchi.config.SecurityConfig;
import com.taskgotchi.dto.TaskRequest;
import com.taskgotchi.dto.TaskResponse;
import com.taskgotchi.model.TaskDifficulty;
import com.taskgotchi.model.TaskPriority;
import com.taskgotchi.model.TaskStatus;
import com.taskgotchi.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
    void getTasks_Returns200AndPage() throws Exception {
        when(taskService.getTasks(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createTask_ValidRequest_Returns201Created() throws Exception {
        TaskRequest request = new TaskRequest("Nueva Tarea", "Descripción", null, TaskPriority.HIGH, TaskDifficulty.MEDIUM, null, null, null);
        TaskResponse response = new TaskResponse(1L, "Nueva Tarea", "Descripción", null, TaskPriority.HIGH, TaskDifficulty.MEDIUM, null, TaskStatus.PENDING, 50, 25, null, null, null);

        when(taskService.createTask(any(TaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("Nueva Tarea"));
    }

    @Test
    void createTask_InvalidRequest_Returns400BadRequest() throws Exception {
        TaskRequest invalidRequest = new TaskRequest("", "Descripción", null, null, null, null, null, null);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    void completeTask_Returns200Ok() throws Exception {
        TaskResponse response = new TaskResponse(1L, "Nueva Tarea", "Descripción", null, TaskPriority.HIGH, TaskDifficulty.MEDIUM, null, TaskStatus.COMPLETED, 50, 25, null, null, null);
        when(taskService.completeTask(eq(1L))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tasks/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
