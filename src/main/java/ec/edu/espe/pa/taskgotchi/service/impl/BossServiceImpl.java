package ec.edu.espe.pa.taskgotchi.service.impl;

import ec.edu.espe.pa.taskgotchi.dto.*;
import ec.edu.espe.pa.taskgotchi.entity.*;
import ec.edu.espe.pa.taskgotchi.exception.BusinessRuleException;
import ec.edu.espe.pa.taskgotchi.exception.ResourceNotFoundException;
import ec.edu.espe.pa.taskgotchi.mapper.BossMapper;
import ec.edu.espe.pa.taskgotchi.repository.*;
import ec.edu.espe.pa.taskgotchi.security.CurrentUserProvider;
import ec.edu.espe.pa.taskgotchi.service.BossService;
import ec.edu.espe.pa.taskgotchi.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BossServiceImpl implements BossService {
    private final BossRepository bossRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final BossMapper bossMapper;
    private final TaskService taskService;

    public List<BossResponse> getAllBosses() {
        return bossRepository.findByUserId(currentUserProvider.getCurrentUserId()).stream()
                .map(boss -> bossMapper.toResponse(boss, taskRepository.findByBossId(boss.getId()))).toList();
    }

    public BossResponse getBossById(Long id) {
        return bossMapper.toResponse(findBoss(id), taskRepository.findByBossId(id));
    }

    @Transactional
    public BossResponse createBoss(CreateBossRequest request) {
        User user = userRepository.findById(currentUserProvider.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Boss boss = bossMapper.toEntity(request);
        boss.setUser(user);
        boss = bossRepository.saveAndFlush(boss);
        if (request.subtasks() != null) {
            for (TaskRequest task : request.subtasks()) addSubtaskToBoss(boss.getId(), task);
        }
        return getBossById(boss.getId());
    }

    @Transactional
    public TaskResponse addSubtaskToBoss(Long bossId, TaskRequest request) {
        Boss boss = findBoss(bossId);
        if (boss.getStatus() == BossStatus.DEFEATED) {
            throw new BusinessRuleException("No se pueden agregar misiones a un jefe derrotado");
        }
        return taskService.createTask(new TaskRequest(request.code(), request.title(), request.description(),
                request.estimatedHours(), request.active(), request.categoryId(), request.priority(),
                request.difficulty(), request.dueDate(), bossId, request.parentTaskId()));
    }

    @Transactional
    public TaskResponse completeBossSubtask(Long bossId, Long subtaskId) {
        findBoss(bossId);
        Task task = taskRepository.findByIdAndUserId(subtaskId, currentUserProvider.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Subtarea no encontrada"));
        if (task.getBoss() == null || !bossId.equals(task.getBoss().getId())) {
            throw new BusinessRuleException("La subtarea no está asociada a este jefe");
        }
        return taskService.completeTask(subtaskId);
    }

    private Boss findBoss(Long id) {
        return bossRepository.findByIdAndUserId(id, currentUserProvider.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Jefe no encontrado"));
    }
}
