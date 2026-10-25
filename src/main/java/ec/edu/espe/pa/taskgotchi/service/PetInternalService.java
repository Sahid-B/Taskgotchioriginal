package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.entity.Pet;
import ec.edu.espe.pa.taskgotchi.entity.PetEventType;

public interface PetInternalService {
    Pet addXpAndCoins(Long userId, int xp, int coins, String eventReason);
}
