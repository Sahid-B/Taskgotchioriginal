package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.model.Pet;
import ec.edu.espe.pa.taskgotchi.model.PetEventType;

public interface PetInternalService {
    Pet addXpAndCoins(Long userId, int xp, int coins, String eventReason);
}
