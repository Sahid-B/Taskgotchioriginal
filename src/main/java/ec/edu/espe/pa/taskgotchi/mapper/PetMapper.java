package ec.edu.espe.pa.taskgotchi.mapper;

import ec.edu.espe.pa.taskgotchi.dto.PetEventResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetResponse;
import ec.edu.espe.pa.taskgotchi.entity.Pet;
import ec.edu.espe.pa.taskgotchi.entity.PetEvent;
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
