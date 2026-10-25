package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.mapper.PetMapper;
import ec.edu.espe.pa.taskgotchi.model.Pet;
import ec.edu.espe.pa.taskgotchi.model.PetStage;
import ec.edu.espe.pa.taskgotchi.model.User;
import ec.edu.espe.pa.taskgotchi.repository.PetEventRepository;
import ec.edu.espe.pa.taskgotchi.repository.PetRepository;
import ec.edu.espe.pa.taskgotchi.repository.UserRepository;
import ec.edu.espe.pa.taskgotchi.security.CurrentUserProvider;
import ec.edu.espe.pa.taskgotchi.service.impl.PetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;
    @Mock
    private PetEventRepository petEventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private PetMapper petMapper;

    @InjectMocks
    private PetServiceImpl petService;

    private User user;
    private Pet pet;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(petService, "babyThreshold", 100);
        ReflectionTestUtils.setField(petService, "youngThreshold", 300);
        ReflectionTestUtils.setField(petService, "adultThreshold", 700);
        ReflectionTestUtils.setField(petService, "legendaryThreshold", 1500);

        user = User.builder().id(1L).username("testuser").email("test@example.com").build();
        pet = Pet.builder()
                .id(100L)
                .user(user)
                .name("Gotchi de testuser")
                .stage(PetStage.EGG)
                .hunger(80)
                .mood(80)
                .energy(80)
                .xp(80)
                .coins(50)
                .lastUpdatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void addXpAndCoins_TriggersEvolution_WhenThresholdReached() {
        when(petRepository.findByUserId(1L)).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pet updatedPet = petService.addXpAndCoins(1L, 50, 20, "Tarea completada");

        assertEquals(130, updatedPet.getXp());
        assertEquals(70, updatedPet.getCoins());
        assertEquals(PetStage.BABY, updatedPet.getStage());
        verify(petEventRepository, times(2)).save(any());
    }
}
