package ec.edu.espe.pa.taskgotchi.config;

import ec.edu.espe.pa.taskgotchi.model.Pet;
import ec.edu.espe.pa.taskgotchi.model.User;
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
                        .id(1L)
                        .username("devstudent")
                        .email("student@espe.edu.ec")
                        .passwordHash(passwordEncoder.encode("devpassword123"))
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
