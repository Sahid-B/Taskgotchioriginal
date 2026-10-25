package ec.edu.espe.pa.taskgotchi.dto;

import ec.edu.espe.pa.taskgotchi.entity.PetEventType;

import java.time.LocalDateTime;

public record PetEventResponse(
        Long id,
        PetEventType type,
        String description,
        LocalDateTime createdAt
) {}
