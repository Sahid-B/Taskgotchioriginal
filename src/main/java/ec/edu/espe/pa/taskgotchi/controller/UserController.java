package ec.edu.espe.pa.taskgotchi.controller;

import ec.edu.espe.pa.taskgotchi.dto.UpdateUserRequest;
import ec.edu.espe.pa.taskgotchi.dto.AvatarDto;
import ec.edu.espe.pa.taskgotchi.dto.UserResponse;
import ec.edu.espe.pa.taskgotchi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Endpoints para la gestión del perfil del usuario autenticado")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil actual", description = "Devuelve los datos del usuario actualmente autenticado.")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PutMapping("/me/avatar")
    @Operation(summary = "Actualizar avatar", description = "Actualiza el género y los colores del personaje del usuario autenticado.")
    public ResponseEntity<UserResponse> updateAvatar(@Valid @RequestBody AvatarDto avatar) {
        return ResponseEntity.ok(userService.updateAvatar(avatar));
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar perfil actual", description = "Actualiza el nombre de usuario o correo electrónico del usuario autenticado.")
    public ResponseEntity<UserResponse> updateCurrentUser(@Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateCurrentUser(request));
    }
}
