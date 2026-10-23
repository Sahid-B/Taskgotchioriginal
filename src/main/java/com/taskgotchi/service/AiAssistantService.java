package com.taskgotchi.service;

import com.taskgotchi.dto.AssistantMessageResponse;
import com.taskgotchi.dto.DailyMissionResponse;
import com.taskgotchi.dto.PetMessageRequest;
import com.taskgotchi.dto.SplitTaskRequest;
import com.taskgotchi.dto.SplitTaskResponse;

import java.util.List;

public interface AiAssistantService {
    AssistantMessageResponse getPetMessage(PetMessageRequest request);
    SplitTaskResponse splitTask(SplitTaskRequest request);
    List<DailyMissionResponse> getDailyMissions();
}
