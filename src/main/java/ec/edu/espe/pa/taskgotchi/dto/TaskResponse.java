package ec.edu.espe.pa.taskgotchi.dto;

import ec.edu.espe.pa.taskgotchi.entity.TaskDifficulty;
import ec.edu.espe.pa.taskgotchi.entity.TaskPriority;
import ec.edu.espe.pa.taskgotchi.entity.TaskStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String code,
        String title,
        String description,
        BigDecimal estimatedHours,
        Boolean active,
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
