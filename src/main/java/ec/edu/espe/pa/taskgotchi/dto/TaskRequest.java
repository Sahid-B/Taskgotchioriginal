package ec.edu.espe.pa.taskgotchi.dto;

import ec.edu.espe.pa.taskgotchi.entity.TaskDifficulty;
import ec.edu.espe.pa.taskgotchi.entity.TaskPriority;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TaskRequest(
        @NotBlank(message = "El código de la tarea es obligatorio")
        @Size(max = 20, message = "El código de la tarea no puede superar los 20 caracteres")
        String code,

        @NotBlank(message = "El título de la tarea es obligatorio")
        @Size(min = 3, max = 100, message = "El título debe tener entre 3 y 100 caracteres")
        String title,

        String description,

        @NotNull(message = "Las horas estimadas son obligatorias")
        @DecimalMin(value = "0.01", message = "Las horas estimadas deben ser al menos 0.01")
        @Digits(integer = 3, fraction = 2, message = "Las horas admiten hasta 3 enteros y 2 decimales")
        BigDecimal estimatedHours,

        Boolean active,

        Long categoryId,

        @NotNull(message = "La prioridad de la tarea es obligatoria")
        TaskPriority priority,

        @NotNull(message = "La dificultad de la tarea es obligatoria")
        TaskDifficulty difficulty,

        LocalDateTime dueDate,

        Long bossId,

        Long parentTaskId
) {}
