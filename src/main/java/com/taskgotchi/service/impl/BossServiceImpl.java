package com.taskgotchi.service.impl;

import com.taskgotchi.dto.BossResponse;
import com.taskgotchi.dto.CreateBossRequest;
import com.taskgotchi.dto.TaskRequest;
import com.taskgotchi.dto.TaskResponse;
import com.taskgotchi.exception.BusinessRuleException;
import com.taskgotchi.exception.ResourceNotFoundException;
import com.taskgotchi.mapper.BossMapper;
import com.taskgotchi.mapper.TaskMapper;
import com.taskgotchi.model.*;
import com.taskgotchi.repository.BossRepository;
import com.taskgotchi.repository.TaskRepository;
import com.taskgotchi.repository.UserRepository;
import com.taskgotchi.security.CurrentUserProvider;
import com.taskgotchi.service.BossService;
import com.taskgotchi.service.PetInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BossServiceImpl implements BossService {

    private final BossRepository bossRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final BossMapper bossMapper;
    private final TaskMapper taskMapper;
    private final PetInternalService petInternalService;

    @Override
    @Transactional(readOnly = true)
    public List<BossResponse> getAllBosses() {
        Long userId = currentUserProvider.getCurrentUserId();
        return bossRepository.findByUserId(userId).stream()
                .map(boss -> {
                    List<Task> subtasks = taskRepository.findByBossId(boss.getId());
                    return bossMapper.toResponse(boss, subtasks);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BossResponse getBossById(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Boss boss = bossRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Jefe no encontrado o no pertenece al usuario"));
        List<Task> subtasks = taskRepository.findByBossId(boss.getId());
        return bossMapper.toResponse(boss, subtasks);
    }

    @Override
    @Transactional
    public BossResponse createBoss(CreateBossRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Boss boss = bossMapper.toEntity(request);
        boss.setUser(user);
        boss = bossRepository.save(boss);

        List<Task> createdSubtasks = List.of();
        if (request.subtasks() != null && !request.subtasks().isEmpty()) {
            final Boss savedBoss = boss;
            createdSubtasks = request.subtasks().stream().map(subReq -> {
                Task task = taskMapper.toEntity(subReq);
                task.setUser(user);
                task.setBoss(savedBoss);

                int xpReward = switch (subReq.difficulty() != null ? subReq.difficulty() : TaskDifficulty.MEDIUM) {
                    case EASY -> 20;
                    case MEDIUM -> 50;
                    case HARD -> 100;
                };
                int coinReward = switch (subReq.difficulty() != null ? subReq.difficulty() : TaskDifficulty.MEDIUM) {
                    case EASY -> 10;
                    case MEDIUM -> 25;
                    case HARD -> 60;
                };
                task.setXpReward(xpReward);
                task.setCoinReward(coinReward);

                return taskRepository.save(task);
            }).toList();
        }

        return bossMapper.toResponse(boss, createdSubtasks);
    }

    @Override
    @Transactional
    public TaskResponse addSubtaskToBoss(Long bossId, TaskRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        Boss boss = bossRepository.findByIdAndUserId(bossId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Jefe no encontrado o no pertenece al usuario"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Task task = taskMapper.toEntity(request);
        task.setUser(user);
        task.setBoss(boss);

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
    public TaskResponse completeBossSubtask(Long bossId, Long subtaskId) {
        Long userId = currentUserProvider.getCurrentUserId();
        Boss boss = bossRepository.findByIdAndUserId(bossId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Jefe no encontrado o no pertenece al usuario"));

        Task subtask = taskRepository.findByIdAndUserId(subtaskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtarea no encontrada o no pertenece al usuario"));

        if (!boss.getId().equals(subtask.getBoss() != null ? subtask.getBoss().getId() : null)) {
            throw new BusinessRuleException("La subtarea no está asociada a este jefe");
        }

        if (subtask.getStatus() == TaskStatus.COMPLETED) {
            throw new BusinessRuleException("La subtarea ya ha sido completada");
        }

        subtask.setStatus(TaskStatus.COMPLETED);
        subtask.setCompletedAt(java.time.LocalDateTime.now());
        subtask = taskRepository.save(subtask);

        // Otorgar recompensas individuales de la subtarea
        petInternalService.addXpAndCoins(userId, subtask.getXpReward(), subtask.getCoinReward(), "Subtarea de jefe completada: " + subtask.getTitle());

        // Reducir vida del jefe
        if (boss.getStatus() == BossStatus.ACTIVE) {
            int damage = subtask.getXpReward();
            int newHp = Math.max(0, boss.getCurrentHp() - damage);
            boss.setCurrentHp(newHp);

            if (newHp == 0) {
                boss.setStatus(BossStatus.DEFEATED);
                petInternalService.addXpAndCoins(userId, 200, 150, "¡Jefe Derrotado: " + boss.getTitle() + "!");
            }
            bossRepository.save(boss);
        }

        return taskMapper.toResponse(subtask);
    }
}
