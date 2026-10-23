package com.taskgotchi.service;

import com.taskgotchi.dto.HabitLogResponse;
import com.taskgotchi.dto.HabitRequest;
import com.taskgotchi.dto.HabitResponse;

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
