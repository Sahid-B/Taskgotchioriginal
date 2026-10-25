package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.dto.TaskRequest;
import ec.edu.espe.pa.taskgotchi.dto.TaskResponse;
import ec.edu.espe.pa.taskgotchi.exception.BusinessRuleException;
import ec.edu.espe.pa.taskgotchi.exception.DuplicateResourceException;
import ec.edu.espe.pa.taskgotchi.mapper.TaskMapper;
import ec.edu.espe.pa.taskgotchi.entity.*;
import ec.edu.espe.pa.taskgotchi.repository.BossRepository;
import ec.edu.espe.pa.taskgotchi.repository.CategoryRepository;
import ec.edu.espe.pa.taskgotchi.repository.TaskRepository;
import ec.edu.espe.pa.taskgotchi.repository.UserRepository;
import ec.edu.espe.pa.taskgotchi.security.CurrentUserProvider;
import ec.edu.espe.pa.taskgotchi.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BossRepository bossRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private PetInternalService petInternalService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").email("test@example.com").build();
        task = Task.builder()
                .id(10L)
                .user(user)
                .code("TSK-100")
                .title("Estudiar para examen")
                .estimatedHours(new BigDecimal("2.50"))
                .active(true)
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.HIGH)
                .difficulty(TaskDifficulty.MEDIUM)
                .xpReward(50)
                .coinReward(25)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createTask_DuplicateCode_ThrowsDuplicateResourceException() {
        TaskRequest request = new TaskRequest("TSK-100", "Estudiar para examen", "Desc", new BigDecimal("2.5"), true, null, TaskPriority.HIGH, TaskDifficulty.MEDIUM, null, null, null);
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.existsByCodeAndUserId("TSK-100", 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> taskService.createTask(request));
    }

    @Test
    void completeTask_Success_GrantsRewards() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(taskRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(task));
        when(taskRepository.saveAndFlush(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(new TaskResponse(
                10L, "TSK-100", "Estudiar para examen", null, new BigDecimal("2.50"), true, null, TaskPriority.HIGH, TaskDifficulty.MEDIUM,
                null, TaskStatus.COMPLETED, 50, 25, null, LocalDateTime.now(), LocalDateTime.now(), null, null
        ));

        TaskResponse response = taskService.completeTask(10L);

        assertNotNull(response);
        assertEquals(TaskStatus.COMPLETED, response.status());
        verify(petInternalService, times(1)).addXpAndCoins(eq(1L), eq(50), eq(25), contains("Estudiar para examen"));
    }

    @Test
    void completeTask_AlreadyCompleted_ThrowsBusinessRuleException() {
        task.setStatus(TaskStatus.COMPLETED);
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(taskRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(task));

        assertThrows(BusinessRuleException.class, () -> taskService.completeTask(10L));
        verify(petInternalService, never()).addXpAndCoins(anyLong(), anyInt(), anyInt(), anyString());
    }
}
