package ec.edu.espe.pa.taskgotchi.dto;

import jakarta.validation.constraints.NotBlank;

public record HabitRequest(
        @NotBlank(message = "El nombre del hábito es obligatorio")
        String name,

        String frequency
) {}
