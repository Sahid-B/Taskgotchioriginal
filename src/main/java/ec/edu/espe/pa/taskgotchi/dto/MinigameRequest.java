package ec.edu.espe.pa.taskgotchi.dto;

import jakarta.validation.constraints.*;

public record MinigameRequest(
        @NotBlank(message = "El minijuego es obligatorio")
        @Pattern(regexp = "^(CATCH|MEMORY|ECHO)$", message = "El minijuego debe ser CATCH, MEMORY o ECHO")
        String game,
        @NotNull(message = "La puntuación es obligatoria")
        @Min(value = 0, message = "La puntuación debe ser al menos 0")
        @Max(value = 100, message = "La puntuación no puede superar 100")
        Integer score
) {}
