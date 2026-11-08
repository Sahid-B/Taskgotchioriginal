package ec.edu.espe.pa.taskgotchi.dto;

import jakarta.validation.constraints.*;

public record BuyFoodRequest(
        @NotBlank(message = "Selecciona una comida") String foodId,
        @NotNull(message = "Indica la cantidad")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 10, message = "La cantidad máxima es 10") Integer quantity) {}
