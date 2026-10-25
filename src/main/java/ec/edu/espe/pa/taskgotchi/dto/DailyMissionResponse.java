package ec.edu.espe.pa.taskgotchi.dto;

public record DailyMissionResponse(
        String id,
        String title,
        String description,
        Integer xpReward,
        Integer coinReward,
        Boolean completed
) {}
