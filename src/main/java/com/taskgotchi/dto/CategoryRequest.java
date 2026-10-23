package com.taskgotchi.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        String name,

        @NotBlank(message = "El color de la categoría es obligatorio")
        String color
) {}
