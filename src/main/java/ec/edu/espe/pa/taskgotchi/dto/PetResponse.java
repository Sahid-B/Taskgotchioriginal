package ec.edu.espe.pa.taskgotchi.dto;

import ec.edu.espe.pa.taskgotchi.model.PetStage;

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
