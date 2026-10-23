package com.taskgotchi.dto;

import java.util.List;

public record SplitTaskResponse(
        String originalTask,
        List<String> suggestedSubtasks
) {}
