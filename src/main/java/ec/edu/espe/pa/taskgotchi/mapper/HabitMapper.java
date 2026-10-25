package ec.edu.espe.pa.taskgotchi.mapper;

import ec.edu.espe.pa.taskgotchi.dto.HabitLogResponse;
import ec.edu.espe.pa.taskgotchi.dto.HabitRequest;
import ec.edu.espe.pa.taskgotchi.dto.HabitResponse;
import ec.edu.espe.pa.taskgotchi.entity.Habit;
import ec.edu.espe.pa.taskgotchi.entity.HabitLog;
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
