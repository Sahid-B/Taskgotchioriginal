package com.taskgotchi.dto;

public record DashboardSummaryResponse(
        long pendingTasks,
        long overdueTasks,
        long completedTasks,
        int currentStreak,
        PetResponse pet
) {}
