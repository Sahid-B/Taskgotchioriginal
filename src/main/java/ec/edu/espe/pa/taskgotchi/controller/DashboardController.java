package ec.edu.espe.pa.taskgotchi.controller;

import ec.edu.espe.pa.taskgotchi.dto.DashboardSummaryResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetEventResponse;
import ec.edu.espe.pa.taskgotchi.dto.WeeklyStatsResponse;
import ec.edu.espe.pa.taskgotchi.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Panel de Control / Dashboard", description = "Endpoints para resúmenes ejecutivos, estadísticas semanales e historial de la mascota")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Resumen del panel", description = "Devuelve contadores de tareas pendientes, vencidas, racha de hábitos y estado de la mascota.")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/weekly-stats")
    @Operation(summary = "Estadísticas semanales", description = "Devuelve estadísticas de rendimiento durante la semana actual.")
    public ResponseEntity<WeeklyStatsResponse> getWeeklyStats() {
        return ResponseEntity.ok(dashboardService.getWeeklyStats());
    }

    @GetMapping("/pet-history")
    @Operation(summary = "Historial rápido de la mascota", description = "Devuelve los 10 eventos más recientes de la mascota.")
    public ResponseEntity<List<PetEventResponse>> getPetHistory() {
        return ResponseEntity.ok(dashboardService.getPetHistory());
    }
}
