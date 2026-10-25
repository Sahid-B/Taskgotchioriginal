package ec.edu.espe.pa.taskgotchi.dto;

import jakarta.validation.constraints.NotBlank;

public record SplitTaskRequest(
        @NotBlank(message = "El título o descripción del objetivo es obligatorio")
        String taskDescription
) {}
