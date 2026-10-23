package com.taskgotchi.service;

import com.taskgotchi.dto.PetResponse;
import com.taskgotchi.dto.TaskRequest;
import com.taskgotchi.dto.TaskResponse;
import com.taskgotchi.exception.BusinessRuleException;
import com.taskgotchi.mapper.PetMapper;
import com.taskgotchi.mapper.TaskMapper;
import com.taskgotchi.model.*;
import com.taskgotchi.repository.BossRepository;
import com.taskgotchi.repository.CategoryRepository;
import com.taskgotchi.repository.TaskRepository;
import com.taskgotchi.repository.UserRepository;
import com.taskgotchi.security.CurrentUserProvider;
import com.taskgotchi.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
                .title("Estudiar para examen")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.HIGH)
                .difficulty(TaskDifficulty.MEDIUM)
                .xpReward(50)
                .coinReward(25)
                .build();
    }

    @Test
    void completeTask_Success_GrantsRewards() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(taskRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(new TaskResponse(
                10L, "Estudiar para examen", null, null, TaskPriority.HIGH, TaskDifficulty.MEDIUM,
                null, TaskStatus.COMPLETED, 50, 25, null, null, null
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
