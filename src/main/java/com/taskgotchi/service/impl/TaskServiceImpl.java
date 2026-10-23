package com.taskgotchi.service.impl;

import com.taskgotchi.dto.TaskRequest;
import com.taskgotchi.dto.TaskResponse;
import com.taskgotchi.exception.BusinessRuleException;
import com.taskgotchi.exception.ResourceNotFoundException;
import com.taskgotchi.mapper.TaskMapper;
import com.taskgotchi.model.*;
import com.taskgotchi.repository.BossRepository;
import com.taskgotchi.repository.CategoryRepository;
import com.taskgotchi.repository.TaskRepository;
import com.taskgotchi.repository.UserRepository;
import com.taskgotchi.security.CurrentUserProvider;
import com.taskgotchi.service.PetInternalService;
import com.taskgotchi.service.TaskService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final BossRepository bossRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final TaskMapper taskMapper;
    private final PetInternalService petInternalService;

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasks(TaskStatus status, Long categoryId, TaskPriority priority, LocalDateTime dueBefore, Pageable pageable) {
        Long userId = currentUserProvider.getCurrentUserId();

        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (dueBefore != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), dueBefore));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return taskRepository.findAll(spec, pageable).map(task -> {
            // Evaluar de forma dinámica si está OVERDUE antes de mapear
            if (task.getStatus() == TaskStatus.PENDING && task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now())) {
                task.setStatus(TaskStatus.OVERDUE);
            }
            return taskMapper.toResponse(task);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada o no pertenece al usuario"));

        if (task.getStatus() == TaskStatus.PENDING && task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now())) {
            task.setStatus(TaskStatus.OVERDUE);
        }

        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Task task = taskMapper.toEntity(request);
        task.setUser(user);

        if (request.categoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(request.categoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada o no pertenece al usuario"));
            task.setCategory(category);
        }

        if (request.bossId() != null) {
            Boss boss = bossRepository.findByIdAndUserId(request.bossId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Jefe no encontrado o no pertenece al usuario"));
            task.setBoss(boss);
        }

        if (request.parentTaskId() != null) {
            Task parentTask = taskRepository.findByIdAndUserId(request.parentTaskId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tarea padre no encontrada o no pertenece al usuario"));
            task.setParentTask(parentTask);
        }

        // Asignar recompensas según dificultad
        int xpReward = switch (request.difficulty()) {
            case EASY -> 20;
            case MEDIUM -> 50;
            case HARD -> 100;
        };
        int coinReward = switch (request.difficulty()) {
            case EASY -> 10;
            case MEDIUM -> 25;
            case HARD -> 60;
        };
        task.setXpReward(xpReward);
        task.setCoinReward(coinReward);

        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada o no pertenece al usuario"));

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new BusinessRuleException("No se puede editar una tarea que ya ha sido completada");
        }

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setDifficulty(request.difficulty());
        task.setDueDate(request.dueDate());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(request.categoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
            task.setCategory(category);
        } else {
            task.setCategory(null);
        }

        // Actualizar recompensas si cambió la dificultad
        int xpReward = switch (request.difficulty()) {
            case EASY -> 20;
            case MEDIUM -> 50;
            case HARD -> 100;
        };
        int coinReward = switch (request.difficulty()) {
            case EASY -> 10;
            case MEDIUM -> 25;
            case HARD -> 60;
        };
        task.setXpReward(xpReward);
        task.setCoinReward(coinReward);

        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada o no pertenece al usuario"));
        taskRepository.delete(task);
    }

    @Override
    @Transactional
    public TaskResponse completeTask(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada o no pertenece al usuario"));

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new BusinessRuleException("La tarea ya se encuentra completada");
        }

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        task = taskRepository.save(task);

        // Sumar XP y monedas a la mascota del usuario y registrar evento
        String reason = "Completó la tarea: " + task.getTitle();
        petInternalService.addXpAndCoins(userId, task.getXpReward(), task.getCoinReward(), reason);

        // Si la tarea está asociada a un jefe, reducir la vida del jefe
        if (task.getBoss() != null) {
            Boss boss = task.getBoss();
            if (boss.getStatus() == BossStatus.ACTIVE) {
                int damage = task.getXpReward(); // Daño proporcional al XP de la subtarea
                int newHp = Math.max(0, boss.getCurrentHp() - damage);
                boss.setCurrentHp(newHp);

                if (newHp == 0) {
                    boss.setStatus(BossStatus.DEFEATED);
                    // Opcional: Recompensa adicional por derrotar al jefe
                    petInternalService.addXpAndCoins(userId, 150, 100, "¡Derrotó al Jefe " + boss.getTitle() + "!");
                }
                bossRepository.save(boss);
            }
        }

        return taskMapper.toResponse(task);
    }
}
