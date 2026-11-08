package ec.edu.espe.pa.taskgotchi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AvatarDto(
        @NotBlank(message = "El género es obligatorio")
        @Pattern(regexp = "^(MALE|FEMALE)$", message = "El género debe ser MALE o FEMALE")
        String gender,
        @NotBlank(message = "El color de la camisa es obligatorio")
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "El color de la camisa debe ser hexadecimal con formato #RRGGBB")
        String shirtColor,
        @NotBlank(message = "El color del pantalón es obligatorio")
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "El color del pantalón debe ser hexadecimal con formato #RRGGBB")
        String pantsColor,
        @NotBlank(message = "El color del cabello es obligatorio")
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "El color del cabello debe ser hexadecimal con formato #RRGGBB")
        String hairColor
) {
    public static AvatarDto defaults() {
        return new AvatarDto("MALE", "#e3a35c", "#203441", "#283f3c");
    }
}
