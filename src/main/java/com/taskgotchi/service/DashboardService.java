package com.taskgotchi.service;

import com.taskgotchi.dto.DashboardSummaryResponse;
import com.taskgotchi.dto.PetEventResponse;
import com.taskgotchi.dto.WeeklyStatsResponse;

import java.util.List;

public interface DashboardService {
    DashboardSummaryResponse getSummary();
    WeeklyStatsResponse getWeeklyStats();
    List<PetEventResponse> getPetHistory();
}
