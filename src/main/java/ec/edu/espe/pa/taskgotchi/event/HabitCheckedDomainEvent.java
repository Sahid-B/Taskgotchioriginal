package ec.edu.espe.pa.taskgotchi.event;

import java.util.UUID;

public record HabitCheckedDomainEvent(
        UUID eventId,
        Long userId,
        Long habitId,
        String habitName,
        int currentStreak,
        int xpReward,
        int coinReward,
        String requestId,
        boolean evolved,
        String previousStage,
        String newStage
) {}
