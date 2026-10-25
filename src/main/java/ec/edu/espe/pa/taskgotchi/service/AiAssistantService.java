package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.dto.AssistantMessageResponse;
import ec.edu.espe.pa.taskgotchi.dto.DailyMissionResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetMessageRequest;
import ec.edu.espe.pa.taskgotchi.dto.SplitTaskRequest;
import ec.edu.espe.pa.taskgotchi.dto.SplitTaskResponse;

import java.util.List;

public interface AiAssistantService {
    AssistantMessageResponse getPetMessage(PetMessageRequest request);
    SplitTaskResponse splitTask(SplitTaskRequest request);
    List<DailyMissionResponse> getDailyMissions();
}
