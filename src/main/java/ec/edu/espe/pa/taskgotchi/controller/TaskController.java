package ec.edu.espe.pa.taskgotchi.controller;

import ec.edu.espe.pa.taskgotchi.dto.PageResponse;
import ec.edu.espe.pa.taskgotchi.dto.TaskRequest;
import ec.edu.espe.pa.taskgotchi.dto.TaskResponse;
import ec.edu.espe.pa.taskgotchi.model.TaskPriority;
import ec.edu.espe.pa.taskgotchi.model.TaskStatus;
import ec.edu.espe.pa.taskgotchi.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tareas", description = "Endpoints para el CRUD de tareas, filtrado, búsqueda por título y completación con recompensas")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "Listar tareas paginadas y filtradas", description = "Permite listar tareas con paginación estable (page=0, size=10, sort=id,asc) y filtros opcionales.")
    public ResponseEntity<PageResponse<TaskResponse>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueBefore,
            @RequestParam(required = false) String title,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasks(status, categoryId, priority, dueBefore, title, pageable));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar tareas por título", description = "Búsqueda paginada por título sin distinguir mayúsculas de minúsculas. El parámetro 'title' es obligatorio.")
    public ResponseEntity<PageResponse<TaskResponse>> searchTasksByTitle(
            @RequestParam(required = true) String title,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(taskService.searchTasksByTitle(title, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tarea por ID", description = "Devuelve el detalle de una tarea del usuario.")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping
    @Operation(summary = "Crear tarea", description = "Crea una nueva tarea asignándole código único, horas estimadas, dificultad, prioridad y recompensas.")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tarea", description = "Actualiza completamente los datos de una tarea no completada.")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tarea", description = "Elimina una tarea del usuario.")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Completar tarea", description = "Marca la tarea como completada, otorga XP y monedas a la mascota y registra un evento.")
    public ResponseEntity<TaskResponse> completeTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.completeTask(id));
    }
}
