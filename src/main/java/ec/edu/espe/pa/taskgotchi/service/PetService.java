package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.dto.PageResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetEventResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetResponse;
import ec.edu.espe.pa.taskgotchi.dto.BonkResponse;
import ec.edu.espe.pa.taskgotchi.dto.CaressResponse;
import ec.edu.espe.pa.taskgotchi.dto.MinigameRequest;
import ec.edu.espe.pa.taskgotchi.dto.MinigameResultResponse;
import org.springframework.data.domain.Pageable;

public interface PetService extends PetInternalService {
    PetResponse getPetStatus();
    PetResponse feedPet();
    PetResponse playWithPet();
    MinigameResultResponse playMinigame(MinigameRequest request);
    CaressResponse caressPet();
    BonkResponse bonkPet();
    PetResponse restPet();
    PageResponse<PetEventResponse> getPetEvents(Pageable pageable);
}
