package ec.edu.espe.pa.taskgotchi.config;

import ec.edu.espe.pa.taskgotchi.entity.Pet;
import ec.edu.espe.pa.taskgotchi.entity.User;
import ec.edu.espe.pa.taskgotchi.repository.PetRepository;
import ec.edu.espe.pa.taskgotchi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

/**
 * Asegura el usuario de desarrollo "devstudent".
 * Si se define DEV_USER_PASSWORD, esa será su contraseña al crearlo. Para un
 * devstudent que ya existe (p. ej. en PostgreSQL) solo se cambia si además
 * DEV_USER_RESET_PASSWORD=true, de forma explícita y una sola vez. Sin contraseña
 * recibe una aleatoria y no se puede iniciar sesión con él.
 */
@Configuration
@RequiredArgsConstructor
public class DevDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);
    static final String DEV_USERNAME = "devstudent";

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${taskgotchi.dev-user.password:}")
    private String devPassword;

    @Value("${taskgotchi.dev-user.reset-password:false}")
    private boolean resetExistingPassword;

    @Bean
    public CommandLineRunner initDevUser() {
        return args -> {
            boolean hasPassword = devPassword != null && !devPassword.isBlank();
            var existing = userRepository.findByUsername(DEV_USERNAME);
            if (existing.isPresent()) {
                // Nunca se sobrescribe una contraseña existente salvo petición explícita.
                if (hasPassword && resetExistingPassword) {
                    User user = existing.get();
                    user.setPasswordHash(passwordEncoder.encode(devPassword));
                    userRepository.save(user);
                    log.warn("Contraseña de '{}' restablecida por DEV_USER_RESET_PASSWORD=true.", DEV_USERNAME);
                }
                return;
            }
            if (userRepository.existsById(1L)) {
                return;
            }
            User user = userRepository.save(User.builder()
                    .username(DEV_USERNAME)
                    .email("student@espe.edu.ec")
                    .passwordHash(passwordEncoder.encode(hasPassword ? devPassword : UUID.randomUUID().toString()))
                    .build());
            petRepository.save(Pet.builder().user(user).name("Gotchi de " + user.getUsername()).build());
            if (!hasPassword) {
                log.info("Usuario '{}' creado sin contraseña conocida. Define DEV_USER_PASSWORD para usarlo o regístrate.",
                        DEV_USERNAME);
            }
        };
    }
}
