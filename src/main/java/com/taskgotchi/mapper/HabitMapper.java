package com.taskgotchi.mapper;

import com.taskgotchi.dto.HabitLogResponse;
import com.taskgotchi.dto.HabitRequest;
import com.taskgotchi.dto.HabitResponse;
import com.taskgotchi.model.Habit;
import com.taskgotchi.model.HabitLog;
import org.springframework.stereotype.Component;

@Component
public class HabitMapper {

    public HabitResponse toResponse(Habit habit) {
        if (habit == null) return null;
        return new HabitResponse(
                habit.getId(),
                habit.getName(),
                habit.getFrequency(),
                habit.getCurrentStreak(),
                habit.getBestStreak(),
                habit.getLastCompletedDate(),
                habit.getActive()
        );
    }

    public Habit toEntity(HabitRequest request) {
        if (request == null) return null;
        return Habit.builder()
                .name(request.name())
                .frequency(request.frequency() != null ? request.frequency() : "DAILY")
                .build();
    }

    public HabitLogResponse toLogResponse(HabitLog habitLog) {
        if (habitLog == null) return null;
        return new HabitLogResponse(
                habitLog.getId(),
                habitLog.getHabit().getId(),
                habitLog.getDate()
        );
    }
}
