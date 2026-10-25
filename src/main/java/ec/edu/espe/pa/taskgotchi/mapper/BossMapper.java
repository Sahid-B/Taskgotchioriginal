package ec.edu.espe.pa.taskgotchi.mapper;

import ec.edu.espe.pa.taskgotchi.dto.BossResponse;
import ec.edu.espe.pa.taskgotchi.dto.CreateBossRequest;
import ec.edu.espe.pa.taskgotchi.dto.TaskResponse;
import ec.edu.espe.pa.taskgotchi.entity.Boss;
import ec.edu.espe.pa.taskgotchi.entity.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BossMapper {

    private final TaskMapper taskMapper;

    public BossResponse toResponse(Boss boss, List<Task> subtasks) {
        if (boss == null) return null;
        List<TaskResponse> subtaskResponses = subtasks != null ? subtasks.stream().map(taskMapper::toResponse).toList() : List.of();
        return new BossResponse(
                boss.getId(),
                boss.getTitle(),
                boss.getMaxHp(),
                boss.getCurrentHp(),
                boss.getStatus(),
                boss.getRewards(),
                subtaskResponses
        );
    }

    public Boss toEntity(CreateBossRequest request) {
        if (request == null) return null;
        return Boss.builder()
                .title(request.title())
                .maxHp(request.maxHp())
                .currentHp(request.maxHp())
                .rewards(request.rewards())
                .build();
    }
}
