package com.taskgotchi.dto;

import com.taskgotchi.model.TaskDifficulty;
import com.taskgotchi.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TaskRequest(
        @NotBlank(message = "El título de la tarea es obligatorio")
        String title,

        String description,

        Long categoryId,

        @NotNull(message = "La prioridad de la tarea es obligatoria")
        TaskPriority priority,

        @NotNull(message = "La dificultad de la tarea es obligatoria")
        TaskDifficulty difficulty,

        LocalDateTime dueDate,

        Long bossId,

        Long parentTaskId
) {}
