package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.dto.DashboardSummaryResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetEventResponse;
import ec.edu.espe.pa.taskgotchi.dto.WeeklyStatsResponse;

import java.util.List;

public interface DashboardService {
    DashboardSummaryResponse getSummary();
    WeeklyStatsResponse getWeeklyStats();
    List<PetEventResponse> getPetHistory();
}
