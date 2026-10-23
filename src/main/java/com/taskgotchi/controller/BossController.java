package com.taskgotchi.controller;

import com.taskgotchi.dto.BossResponse;
import com.taskgotchi.dto.CreateBossRequest;
import com.taskgotchi.dto.TaskRequest;
import com.taskgotchi.dto.TaskResponse;
import com.taskgotchi.service.BossService;
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
@RequestMapping("/api/v1/bosses")
@RequiredArgsConstructor
@Tag(name = "Jefes Finales", description = "Endpoints para la gestión de proyectos grandes representados como Jefes con barra de vida y subtareas")
public class BossController {

    private final BossService bossService;

    @GetMapping
    @Operation(summary = "Listar jefes", description = "Devuelve todos los jefes activos y derrotados del usuario.")
    public ResponseEntity<List<BossResponse>> getAllBosses() {
        return ResponseEntity.ok(bossService.getAllBosses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener jefe por ID", description = "Devuelve los detalles de un jefe con su barra de vida y lista de subtareas.")
    public ResponseEntity<BossResponse> getBossById(@PathVariable Long id) {
        return ResponseEntity.ok(bossService.getBossById(id));
    }

    @PostMapping
    @Operation(summary = "Crear jefe", description = "Crea un jefe a partir de un objetivo grande con opcional creación inicial de subtareas.")
    public ResponseEntity<BossResponse> createBoss(@Valid @RequestBody CreateBossRequest request) {
        BossResponse response = bossService.createBoss(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{id}/subtasks")
    @Operation(summary = "Agregar subtarea a jefe", description = "Crea y asocia una nueva subtarea al jefe.")
    public ResponseEntity<TaskResponse> addSubtaskToBoss(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        TaskResponse response = bossService.addSubtaskToBoss(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/subtasks/{subId}/complete")
    @Operation(summary = "Completar subtarea de jefe", description = "Completa la subtarea, le resta vida proporcional al jefe y si llega a 0 HP lo marca como derrotado otorgando recompensas.")
    public ResponseEntity<TaskResponse> completeBossSubtask(@PathVariable Long id, @PathVariable Long subId) {
        return ResponseEntity.ok(bossService.completeBossSubtask(id, subId));
    }
}
