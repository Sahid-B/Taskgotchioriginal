package com.taskgotchi.mapper;

import com.taskgotchi.dto.PetEventResponse;
import com.taskgotchi.dto.PetResponse;
import com.taskgotchi.model.Pet;
import com.taskgotchi.model.PetEvent;
import org.springframework.stereotype.Component;

@Component
public class PetMapper {

    public PetResponse toResponse(Pet pet) {
        if (pet == null) return null;
        return new PetResponse(
                pet.getId(),
                pet.getName(),
                pet.getStage(),
                pet.getHunger(),
                pet.getMood(),
                pet.getEnergy(),
                pet.getXp(),
                pet.getCoins(),
                pet.getPersonality(),
                pet.getLastUpdatedAt()
        );
    }

    public PetEventResponse toEventResponse(PetEvent event) {
        if (event == null) return null;
        return new PetEventResponse(
                event.getId(),
                event.getType(),
                event.getDescription(),
                event.getCreatedAt()
        );
    }
}
