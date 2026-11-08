package ec.edu.espe.pa.taskgotchi.dto;

import jakarta.validation.constraints.NotBlank;

public record FeedFoodRequest(@NotBlank(message = "Selecciona una comida") String foodId) {}
