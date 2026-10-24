package com.taskgotchi.dto;

import com.taskgotchi.model.TaskDifficulty;
import com.taskgotchi.model.TaskPriority;
import com.taskgotchi.model.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        CategoryResponse category,
        TaskPriority priority,
        TaskDifficulty difficulty,
        LocalDateTime dueDate,
        TaskStatus status,
        Integer xpReward,
        Integer coinReward,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long bossId,
        Long parentTaskId
) {}
