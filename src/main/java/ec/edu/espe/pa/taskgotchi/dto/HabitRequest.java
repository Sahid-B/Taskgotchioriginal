package ec.edu.espe.pa.taskgotchi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record HabitRequest(
        @NotBlank(message = "El nombre del hábito es obligatorio")
        @Size(min = 3, max = 100)
        String name,

        @Pattern(regexp = "DAILY", message = "Esta versión admite hábitos diarios DAILY") String frequency
) {}
