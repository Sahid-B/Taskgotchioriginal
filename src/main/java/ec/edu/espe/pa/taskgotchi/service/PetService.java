package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.dto.PageResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetEventResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetResponse;
import org.springframework.data.domain.Pageable;

public interface PetService extends PetInternalService {
    PetResponse getPetStatus();
    PetResponse feedPet();
    PetResponse playWithPet();
    PetResponse restPet();
    PageResponse<PetEventResponse> getPetEvents(Pageable pageable);
}
