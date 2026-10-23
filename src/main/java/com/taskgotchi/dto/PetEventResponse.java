package com.taskgotchi.dto;

import com.taskgotchi.model.PetEventType;

import java.time.LocalDateTime;

public record PetEventResponse(
        Long id,
        PetEventType type,
        String description,
        LocalDateTime createdAt
) {}
