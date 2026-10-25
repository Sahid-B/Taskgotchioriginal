package ec.edu.espe.pa.taskgotchi.config;

import ec.edu.espe.pa.taskgotchi.entity.Pet;
import ec.edu.espe.pa.taskgotchi.entity.User;
import ec.edu.espe.pa.taskgotchi.repository.PetRepository;
import ec.edu.espe.pa.taskgotchi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configurador de inicialización de datos de desarrollo para asegurar un usuario dev (ID 1L).
 */
@Configuration
@RequiredArgsConstructor
public class DevDataInitializer {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDevUser() {
        return args -> {
            if (!userRepository.existsById(1L) && !userRepository.existsByUsername("devstudent")) {
                User user = User.builder()
                        .username("devstudent")
                        .email("student@espe.edu.ec")
                        .passwordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                        .build();
                user = userRepository.save(user);

                Pet pet = Pet.builder()
                        .user(user)
                        .name("Gotchi de " + user.getUsername())
                        .build();
                petRepository.save(pet);
            }
        };
    }
}
