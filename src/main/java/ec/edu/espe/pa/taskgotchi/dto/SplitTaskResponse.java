package ec.edu.espe.pa.taskgotchi.dto;

import java.util.List;

public record SplitTaskResponse(
        String originalTask,
        List<String> suggestedSubtasks
) {}
