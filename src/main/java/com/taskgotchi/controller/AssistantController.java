package com.taskgotchi.controller;

import com.taskgotchi.dto.AssistantMessageResponse;
import com.taskgotchi.dto.DailyMissionResponse;
import com.taskgotchi.dto.PetMessageRequest;
import com.taskgotchi.dto.SplitTaskRequest;
import com.taskgotchi.dto.SplitTaskResponse;
import com.taskgotchi.service.AiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assistant")
@RequiredArgsConstructor
@Tag(name = "Asistente IA", description = "Endpoints para interacción con la personalidad de la mascota y descompositor de tareas mediante MOCK IA")
public class AssistantController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/pet-message")
    @Operation(summary = "Obtener mensaje de la mascota", description = "Genera una respuesta con personalidad según el estado actual de la mascota.")
    public ResponseEntity<AssistantMessageResponse> getPetMessage(@RequestBody(required = false) PetMessageRequest request) {
        return ResponseEntity.ok(aiAssistantService.getPetMessage(request));
    }

    @PostMapping("/split-task")
    @Operation(summary = "Descomponer tarea con IA", description = "Recibe un objetivo grande y propone una sugerencia de subtareas paso a paso.")
    public ResponseEntity<SplitTaskResponse> splitTask(@Valid @RequestBody SplitTaskRequest request) {
        return ResponseEntity.ok(aiAssistantService.splitTask(request));
    }

    @GetMapping("/daily-missions")
    @Operation(summary = "Obtener misiones diarias", description = "Devuelve misiones recomendadas por la IA para ganar experiencia y monedas extra hoy.")
    public ResponseEntity<List<DailyMissionResponse>> getDailyMissions() {
        return ResponseEntity.ok(aiAssistantService.getDailyMissions());
    }
}
