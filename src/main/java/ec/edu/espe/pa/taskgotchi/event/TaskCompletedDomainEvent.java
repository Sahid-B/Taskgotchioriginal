package ec.edu.espe.pa.taskgotchi.event;

import java.util.UUID;

public record TaskCompletedDomainEvent(
        UUID eventId,
        Long userId,
        Long taskId,
        String taskTitle,
        int xpReward,
        int coinReward,
        String requestId,
        boolean evolved,
        String previousStage,
        String newStage,
        boolean bossDefeated,
        String bossTitle,
        int bossBonusXp,
        int bossBonusCoins
) {}
