package ec.edu.espe.pa.taskgotchi.dto;

import java.time.LocalDate;

public record HabitResponse(
        Long id,
        String name,
        String frequency,
        Integer currentStreak,
        Integer bestStreak,
        LocalDate lastCompletedDate,
        Boolean active
) {}
