package ec.edu.espe.pa.taskgotchi.service.impl;

import ec.edu.espe.pa.taskgotchi.dto.PageResponse;
import ec.edu.espe.pa.taskgotchi.dto.TaskRequest;
import ec.edu.espe.pa.taskgotchi.dto.TaskResponse;
import ec.edu.espe.pa.taskgotchi.exception.BusinessRuleException;
import ec.edu.espe.pa.taskgotchi.exception.DuplicateResourceException;
import ec.edu.espe.pa.taskgotchi.exception.ResourceNotFoundException;
import ec.edu.espe.pa.taskgotchi.mapper.TaskMapper;
import ec.edu.espe.pa.taskgotchi.model.*;
import ec.edu.espe.pa.taskgotchi.repository.BossRepository;
import ec.edu.espe.pa.taskgotchi.repository.CategoryRepository;
import ec.edu.espe.pa.taskgotchi.repository.TaskRepository;
import ec.edu.espe.pa.taskgotchi.repository.UserRepository;
import ec.edu.espe.pa.taskgotchi.security.CurrentUserProvider;
import ec.edu.espe.pa.taskgotchi.service.PetInternalService;
import ec.edu.espe.pa.taskgotchi.service.TaskService;
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
    public PageResponse<TaskResponse> getTasks(TaskStatus status, Long categoryId, TaskPriority priority, LocalDateTime dueBefore, String title, Pageable pageable) {
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
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase().trim() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TaskResponse> mappedPage = taskRepository.findAll(spec, pageable).map(task -> {
            if (task.getStatus() == TaskStatus.PENDING && task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now())) {
                task.setStatus(TaskStatus.OVERDUE);
            }
            return taskMapper.toResponse(task);
        });

        return PageResponse.from(mappedPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> searchTasksByTitle(String title, Pageable pageable) {
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessRuleException("El parámetro de búsqueda 'title' no puede estar vacío");
        }
        return getTasks(null, null, null, null, title, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));

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
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        String normalizedCode = request.code().trim().toUpperCase();
        if (taskRepository.existsByCodeAndUserId(normalizedCode, userId)) {
            throw new DuplicateResourceException("Ya existe una tarea registrada con el código: " + normalizedCode);
        }

        Task task = taskMapper.toEntity(request);
        task.setCode(normalizedCode);
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

        task = taskRepository.saveAndFlush(task);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new BusinessRuleException("No se puede editar una tarea que ya ha sido completada");
        }

        String normalizedCode = request.code().trim().toUpperCase();
        if (taskRepository.existsByCodeAndUserIdAndIdNot(normalizedCode, userId, id)) {
            throw new DuplicateResourceException("Ya existe otra tarea registrada con el código: " + normalizedCode);
        }

        task.setCode(normalizedCode);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setEstimatedHours(request.estimatedHours());
        task.setActive(request.active() != null ? request.active() : true);
        task.setPriority(request.priority());
        task.setDifficulty(request.difficulty());
        task.setDueDate(request.dueDate());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(request.categoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada o no pertenece al usuario"));
            task.setCategory(category);
        } else {
            task.setCategory(null);
        }

        if (request.bossId() != null) {
            Boss boss = bossRepository.findByIdAndUserId(request.bossId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Jefe no encontrado o no pertenece al usuario"));
            task.setBoss(boss);
        } else {
            task.setBoss(null);
        }

        if (request.parentTaskId() != null) {
            if (request.parentTaskId().equals(id)) {
                throw new BusinessRuleException("Una tarea no puede ser su propia tarea padre");
            }
            Task parentTask = taskRepository.findByIdAndUserId(request.parentTaskId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tarea padre no encontrada o no pertenece al usuario"));

            Task currentAncestor = parentTask;
            while (currentAncestor != null) {
                if (currentAncestor.getId().equals(id)) {
                    throw new BusinessRuleException("No se permite crear ciclos de jerarquía entre tareas");
                }
                currentAncestor = currentAncestor.getParentTask();
            }

            task.setParentTask(parentTask);
        } else {
            task.setParentTask(null);
        }

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

        task = taskRepository.saveAndFlush(task);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));
        taskRepository.delete(task);
    }

    @Override
    @Transactional
    public TaskResponse completeTask(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new BusinessRuleException("La tarea ya se encuentra completada");
        }

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        task = taskRepository.saveAndFlush(task);

        String reason = "Completó la tarea: " + task.getTitle();
        petInternalService.addXpAndCoins(userId, task.getXpReward(), task.getCoinReward(), reason);

        if (task.getBoss() != null) {
            Boss boss = task.getBoss();
            if (boss.getStatus() == BossStatus.ACTIVE) {
                int damage = task.getXpReward();
                int newHp = Math.max(0, boss.getCurrentHp() - damage);
                boss.setCurrentHp(newHp);

                if (newHp == 0) {
                    boss.setStatus(BossStatus.DEFEATED);
                    petInternalService.addXpAndCoins(userId, 150, 100, "¡Derrotó al Jefe " + boss.getTitle() + "!");
                }
                bossRepository.save(boss);
            }
        }

        return taskMapper.toResponse(task);
    }
}
