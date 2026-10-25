package ec.edu.espe.pa.taskgotchi.service;

import ec.edu.espe.pa.taskgotchi.dto.PageResponse;
import ec.edu.espe.pa.taskgotchi.dto.TaskRequest;
import ec.edu.espe.pa.taskgotchi.dto.TaskResponse;
import ec.edu.espe.pa.taskgotchi.model.TaskPriority;
import ec.edu.espe.pa.taskgotchi.model.TaskStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface TaskService {
    PageResponse<TaskResponse> getTasks(TaskStatus status, Long categoryId, TaskPriority priority, LocalDateTime dueBefore, String title, Pageable pageable);
    PageResponse<TaskResponse> searchTasksByTitle(String title, Pageable pageable);
    TaskResponse getTaskById(Long id);
    TaskResponse createTask(TaskRequest request);
    TaskResponse updateTask(Long id, TaskRequest request);
    void deleteTask(Long id);
    TaskResponse completeTask(Long id);
}
