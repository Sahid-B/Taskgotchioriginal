package ec.edu.espe.pa.taskgotchi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateBossRequest(
        @NotBlank(message = "El título del jefe es obligatorio")
        @Size(max = 100)
        String title,

        @NotNull(message = "La vida máxima del jefe es obligatoria")
        @Positive(message = "La vida debe ser mayor que cero")
        Integer maxHp,

        String rewards,

        List<@Valid TaskRequest> subtasks
) {}
