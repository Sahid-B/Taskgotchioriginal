package com.taskgotchi.dto;

import com.taskgotchi.model.BossStatus;

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
