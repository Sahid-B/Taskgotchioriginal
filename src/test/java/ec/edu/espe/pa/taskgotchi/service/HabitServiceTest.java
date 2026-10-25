package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.dto.HabitResponse;
import ec.edu.espe.pa.taskgotchi.exception.BusinessRuleException;
import ec.edu.espe.pa.taskgotchi.mapper.HabitMapper;
import ec.edu.espe.pa.taskgotchi.model.Habit;
import ec.edu.espe.pa.taskgotchi.model.User;
import ec.edu.espe.pa.taskgotchi.repository.HabitLogRepository;
import ec.edu.espe.pa.taskgotchi.repository.HabitRepository;
import ec.edu.espe.pa.taskgotchi.repository.UserRepository;
import ec.edu.espe.pa.taskgotchi.security.CurrentUserProvider;
import ec.edu.espe.pa.taskgotchi.service.impl.HabitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {

    @Mock
    private HabitRepository habitRepository;
    @Mock
    private HabitLogRepository habitLogRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private HabitMapper habitMapper;
    @Mock
    private PetInternalService petInternalService;

    @InjectMocks
    private HabitServiceImpl habitService;

    private User user;
    private Habit habit;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();
        habit = Habit.builder()
                .id(5L)
                .user(user)
                .name("Ejercicio")
                .frequency("DAILY")
                .currentStreak(2)
                .bestStreak(2)
                .lastCompletedDate(LocalDate.now().minusDays(1))
                .active(true)
                .build();
    }

    @Test
    void checkHabit_Success_IncrementsStreakAndGrantsReward() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(habitRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(habit));
        when(habitLogRepository.existsByHabitIdAndDate(eq(5L), any(LocalDate.class))).thenReturn(false);
        when(habitRepository.save(any(Habit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(habitMapper.toResponse(any(Habit.class))).thenReturn(new HabitResponse(
                5L, "Ejercicio", "DAILY", 3, 3, LocalDate.now(), true
        ));

        HabitResponse response = habitService.checkHabit(5L);

        assertNotNull(response);
        assertEquals(3, response.currentStreak());
        assertEquals(3, response.bestStreak());
        verify(petInternalService, times(1)).addXpAndCoins(eq(1L), eq(30), eq(10), contains("Ejercicio"));
    }

    @Test
    void checkHabit_AlreadyCheckedToday_ThrowsException() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(habitRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(habit));
        when(habitLogRepository.existsByHabitIdAndDate(eq(5L), any(LocalDate.class))).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> habitService.checkHabit(5L));
    }
}
