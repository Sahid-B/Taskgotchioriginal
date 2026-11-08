package ec.edu.espe.pa.taskgotchi.controller;

import ec.edu.espe.pa.taskgotchi.dto.PageResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetEventResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetResponse;
import ec.edu.espe.pa.taskgotchi.dto.BonkResponse;
import ec.edu.espe.pa.taskgotchi.dto.CaressResponse;
import ec.edu.espe.pa.taskgotchi.dto.MinigameRequest;
import ec.edu.espe.pa.taskgotchi.dto.MinigameResultResponse;
import jakarta.validation.Valid;
import ec.edu.espe.pa.taskgotchi.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pet")
@RequiredArgsConstructor
@Tag(name = "Mascota Virtual", description = "Endpoints para consultar estado, interactuar (alimentar, jugar, descansar) e historial de la mascota")
public class PetController {

    private final PetService petService;

    @GetMapping
    @Operation(summary = "Obtener estado de la mascota", description = "Devuelve el estado actual de la mascota calculando el desgaste de estadísticas por tiempo.")
    public ResponseEntity<PetResponse> getPetStatus() {
        return ResponseEntity.ok(petService.getPetStatus());
    }

    @PostMapping("/feed")
    @Operation(summary = "Alimentar mascota", description = "Aumenta el nivel de hambre y ánimo consumiendo monedas.")
    public ResponseEntity<PetResponse> feedPet() {
        return ResponseEntity.ok(petService.feedPet());
    }

    @PostMapping("/play")
    @Operation(summary = "Jugar con la mascota", description = "Aumenta el ánimo consumiendo energía.")
    public ResponseEntity<PetResponse> playWithPet() {
        return ResponseEntity.ok(petService.playWithPet());
    }

    @PostMapping("/caress")
    @Operation(summary = "Acariciar a la mascota", description = "Sube 5 puntos de ánimo como máximo una vez cada 20 segundos. Dentro de la espera no suma, pero responde cuántos segundos faltan.")
    public ResponseEntity<CaressResponse> caressPet() {
        return ResponseEntity.ok(petService.caressPet());
    }

    @PostMapping("/bonk")
    @Operation(summary = "Golpear a la mascota", description = "Baja 5 puntos de ánimo como máximo una vez cada 10 segundos.")
    public ResponseEntity<BonkResponse> bonkPet() {
        return ResponseEntity.ok(petService.bonkPet());
    }

    @PostMapping("/minigame")
    @Operation(summary = "Registrar resultado de un minijuego", description = "Aplica los efectos de jugar y entrega pequeñas recompensas en las primeras ocho partidas del día.")
    public ResponseEntity<MinigameResultResponse> playMinigame(@Valid @RequestBody MinigameRequest request) {
        return ResponseEntity.ok(petService.playMinigame(request));
    }

    @PostMapping("/rest")
    @Operation(summary = "Poner a descansar a la mascota", description = "Recupera los niveles de energía de la mascota.")
    public ResponseEntity<PetResponse> restPet() {
        return ResponseEntity.ok(petService.restPet());
    }

    @GetMapping("/events")
    @Operation(summary = "Obtener historial de eventos", description = "Devuelve el historial paginado de eventos de la mascota (comidas, evoluciones, tareas completadas).")
    public ResponseEntity<PageResponse<PetEventResponse>> getPetEvents(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(petService.getPetEvents(pageable));
    }
}
