package com.taskgotchi.dto;

import com.taskgotchi.model.PetStage;

import java.time.LocalDateTime;

public record PetResponse(
        Long id,
        String name,
        PetStage stage,
        Integer hunger,
        Integer mood,
        Integer energy,
        Integer xp,
        Integer coins,
        String personality,
        LocalDateTime lastUpdatedAt
) {}
