package com.taskgotchi.mapper;

import com.taskgotchi.dto.TaskRequest;
import com.taskgotchi.dto.TaskResponse;
import com.taskgotchi.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final CategoryMapper categoryMapper;

    public TaskResponse toResponse(Task task) {
        if (task == null) return null;
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                categoryMapper.toResponse(task.getCategory()),
                task.getPriority(),
                task.getDifficulty(),
                task.getDueDate(),
                task.getStatus(),
                task.getXpReward(),
                task.getCoinReward(),
                task.getCompletedAt(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getBoss() != null ? task.getBoss().getId() : null,
                task.getParentTask() != null ? task.getParentTask().getId() : null
        );
    }

    public Task toEntity(TaskRequest request) {
        if (request == null) return null;
        return Task.builder()
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .difficulty(request.difficulty())
                .dueDate(request.dueDate())
                .build();
    }
}
