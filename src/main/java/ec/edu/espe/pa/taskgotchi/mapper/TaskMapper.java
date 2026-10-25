package ec.edu.espe.pa.taskgotchi.mapper;

import ec.edu.espe.pa.taskgotchi.dto.TaskRequest;
import ec.edu.espe.pa.taskgotchi.dto.TaskResponse;
import ec.edu.espe.pa.taskgotchi.entity.Task;
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
                task.getCode(),
                task.getTitle(),
                task.getDescription(),
                task.getEstimatedHours(),
                task.getActive(),
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
                .code(request.code() != null ? request.code().trim().toUpperCase() : null)
                .title(request.title().trim())
                .description(request.description())
                .estimatedHours(request.estimatedHours())
                .active(request.active() != null ? request.active() : true)
                .priority(request.priority())
                .difficulty(request.difficulty())
                .dueDate(request.dueDate())
                .build();
    }
}
