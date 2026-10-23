package com.taskgotchi.service;

import com.taskgotchi.dto.PetEventResponse;
import com.taskgotchi.dto.PetResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PetService extends PetInternalService {
    PetResponse getPetStatus();
    PetResponse feedPet();
    PetResponse playWithPet();
    PetResponse restPet();
    Page<PetEventResponse> getPetEvents(Pageable pageable);
}
