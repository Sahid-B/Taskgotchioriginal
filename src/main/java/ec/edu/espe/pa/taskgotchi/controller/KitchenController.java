package ec.edu.espe.pa.taskgotchi.controller;

import ec.edu.espe.pa.taskgotchi.dto.*;
import ec.edu.espe.pa.taskgotchi.service.KitchenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/kitchen")
@RequiredArgsConstructor
@Tag(name = "Cocina")
public class KitchenController {
    private final KitchenService kitchenService;

    @GetMapping("/foods")
    @Operation(summary = "Consultar el catálogo de comidas")
    public ResponseEntity<List<FoodResponse>> getFoods() {
        return ResponseEntity.ok(kitchenService.getFoods());
    }

    @GetMapping("/pantry")
    @Operation(summary = "Consultar la despensa y el estado de la mascota")
    public ResponseEntity<KitchenStateResponse> getPantry() {
        return ResponseEntity.ok(kitchenService.getPantry());
    }

    @PostMapping("/buy")
    @Operation(summary = "Comprar comida con las monedas de la mascota")
    public ResponseEntity<KitchenStateResponse> buyFood(@Valid @RequestBody BuyFoodRequest request) {
        return ResponseEntity.ok(kitchenService.buyFood(request));
    }

    @PostMapping("/feed")
    @Operation(summary = "Alimentar a la mascota con comida de la despensa")
    public ResponseEntity<KitchenStateResponse> feedFood(@Valid @RequestBody FeedFoodRequest request) {
        return ResponseEntity.ok(kitchenService.feedFood(request));
    }
}
