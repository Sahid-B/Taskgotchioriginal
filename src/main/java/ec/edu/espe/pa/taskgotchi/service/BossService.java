package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.dto.BossResponse;
import ec.edu.espe.pa.taskgotchi.dto.CreateBossRequest;
import ec.edu.espe.pa.taskgotchi.dto.TaskRequest;
import ec.edu.espe.pa.taskgotchi.dto.TaskResponse;

import java.util.List;

public interface BossService {
    List<BossResponse> getAllBosses();
    BossResponse getBossById(Long id);
    BossResponse createBoss(CreateBossRequest request);
    TaskResponse addSubtaskToBoss(Long bossId, TaskRequest request);
    TaskResponse completeBossSubtask(Long bossId, Long subtaskId);
}
