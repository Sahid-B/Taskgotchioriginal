package com.taskgotchi.dto;

import java.time.LocalDate;

public record HabitLogResponse(
        Long id,
        Long habitId,
        LocalDate date
) {}
