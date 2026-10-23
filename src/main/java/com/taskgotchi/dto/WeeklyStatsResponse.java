package com.taskgotchi.dto;

import java.util.Map;

public record WeeklyStatsResponse(
        long completedTasksThisWeek,
        long habitsCompletedThisWeek,
        Map<String, Integer> tasksByDay
) {}
