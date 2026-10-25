package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.dto.HabitLogResponse;
import ec.edu.espe.pa.taskgotchi.dto.HabitRequest;
import ec.edu.espe.pa.taskgotchi.dto.HabitResponse;

import java.util.List;

public interface HabitService {
    List<HabitResponse> getAllHabits();
    HabitResponse getHabitById(Long id);
    HabitResponse createHabit(HabitRequest request);
    HabitResponse updateHabit(Long id, HabitRequest request);
    void deleteHabit(Long id);
    HabitResponse checkHabit(Long id);
    List<HabitLogResponse> getHabitLogs(Long id);
}
