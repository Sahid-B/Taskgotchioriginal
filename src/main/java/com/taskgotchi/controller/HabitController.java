package com.taskgotchi.controller;

import com.taskgotchi.dto.HabitLogResponse;
import com.taskgotchi.dto.HabitRequest;
import com.taskgotchi.dto.HabitResponse;
import com.taskgotchi.service.HabitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/habits")
@RequiredArgsConstructor
@Tag(name = "Hábitos", description = "Endpoints para el CRUD de hábitos, registro diario de cumplimiento y rachas")
public class HabitController {

    private final HabitService habitService;

    @GetMapping
    @Operation(summary = "Listar hábitos", description = "Devuelve todos los hábitos del usuario autenticado.")
    public ResponseEntity<List<HabitResponse>> getAllHabits() {
        return ResponseEntity.ok(habitService.getAllHabits());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener hábito por ID", description = "Devuelve los detalles de un hábito específico.")
    public ResponseEntity<HabitResponse> getHabitById(@PathVariable Long id) {
        return ResponseEntity.ok(habitService.getHabitById(id));
    }

    @PostMapping
    @Operation(summary = "Crear hábito", description = "Crea un nuevo hábito para el usuario.")
    public ResponseEntity<HabitResponse> createHabit(@Valid @RequestBody HabitRequest request) {
        HabitResponse response = habitService.createHabit(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar hábito", description = "Modifica los datos de un hábito existente.")
    public ResponseEntity<HabitResponse> updateHabit(@PathVariable Long id, @Valid @RequestBody HabitRequest request) {
        return ResponseEntity.ok(habitService.updateHabit(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar hábito", description = "Elimina un hábito del usuario.")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/check")
    @Operation(summary = "Marcar hábito cumplido hoy", description = "Registra el cumplimiento del hábito para hoy, calcula y actualiza la racha y bonifica a la mascota.")
    public ResponseEntity<HabitResponse> checkHabit(@PathVariable Long id) {
        return ResponseEntity.ok(habitService.checkHabit(id));
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "Obtener historial de registros del hábito", description = "Devuelve el historial de días en los que se cumplió el hábito.")
    public ResponseEntity<List<HabitLogResponse>> getHabitLogs(@PathVariable Long id) {
        return ResponseEntity.ok(habitService.getHabitLogs(id));
    }
}
