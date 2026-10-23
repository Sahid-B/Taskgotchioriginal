package com.taskgotchi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateBossRequest(
        @NotBlank(message = "El título del jefe es obligatorio")
        String title,

        @NotNull(message = "La vida máxima del jefe es obligatoria")
        Integer maxHp,

        String rewards,

        List<TaskRequest> subtasks
) {}
