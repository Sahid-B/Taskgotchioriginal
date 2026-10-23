package com.taskgotchi.service;

import com.taskgotchi.dto.BossResponse;
import com.taskgotchi.dto.CreateBossRequest;
import com.taskgotchi.dto.TaskRequest;
import com.taskgotchi.dto.TaskResponse;

import java.util.List;

public interface BossService {
    List<BossResponse> getAllBosses();
    BossResponse getBossById(Long id);
    BossResponse createBoss(CreateBossRequest request);
    TaskResponse addSubtaskToBoss(Long bossId, TaskRequest request);
    TaskResponse completeBossSubtask(Long bossId, Long subtaskId);
}
