package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.entity.PetEventType;
import java.util.List;

public record RewardOutcome(
        int xpGranted,
        int coinsGranted,
        boolean evolved,
        String previousStage,
        String newStage,
        List<PetEventLog> eventsToLog
) {
    public record PetEventLog(PetEventType type, String description) {}
}
