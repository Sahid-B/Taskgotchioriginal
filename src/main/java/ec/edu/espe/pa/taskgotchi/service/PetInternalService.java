package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.entity.Pet;

public interface PetInternalService {
    Pet getPetWithTimeDecay(Long userId);
    RewardOutcome addXpAndCoins(Long userId, int xp, int coins, String eventReason);
}
