package ec.edu.espe.pa.taskgotchi.dto;

import ec.edu.espe.pa.taskgotchi.entity.BossStatus;

import java.util.List;

public record BossResponse(
        Long id,
        String title,
        Integer maxHp,
        Integer currentHp,
        BossStatus status,
        String rewards,
        List<TaskResponse> subtasks
) {}
