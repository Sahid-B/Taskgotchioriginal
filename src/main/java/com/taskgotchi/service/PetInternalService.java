package com.taskgotchi.service;

import com.taskgotchi.model.Pet;
import com.taskgotchi.model.PetEventType;

public interface PetInternalService {
    Pet addXpAndCoins(Long userId, int xp, int coins, String eventReason);
}
