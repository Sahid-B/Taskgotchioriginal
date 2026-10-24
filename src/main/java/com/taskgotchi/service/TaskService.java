package com.taskgotchi.service;

import com.taskgotchi.dto.TaskRequest;
import com.taskgotchi.dto.TaskResponse;
import com.taskgotchi.model.TaskPriority;
import com.taskgotchi.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface TaskService {
    Page<TaskResponse> getTasks(TaskStatus status, Long categoryId, TaskPriority priority, LocalDateTime dueBefore, String title, Pageable pageable);
    TaskResponse getTaskById(Long id);
    TaskResponse createTask(TaskRequest request);
    TaskResponse updateTask(Long id, TaskRequest request);
    void deleteTask(Long id);
    TaskResponse completeTask(Long id);
}
