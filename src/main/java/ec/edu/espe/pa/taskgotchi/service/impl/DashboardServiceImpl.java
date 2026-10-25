package ec.edu.espe.pa.taskgotchi.service.impl;

import ec.edu.espe.pa.taskgotchi.dto.DashboardSummaryResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetEventResponse;
import ec.edu.espe.pa.taskgotchi.dto.PetResponse;
import ec.edu.espe.pa.taskgotchi.dto.WeeklyStatsResponse;
import ec.edu.espe.pa.taskgotchi.mapper.PetMapper;
import ec.edu.espe.pa.taskgotchi.entity.Habit;
import ec.edu.espe.pa.taskgotchi.entity.Pet;
import ec.edu.espe.pa.taskgotchi.entity.Task;
import ec.edu.espe.pa.taskgotchi.entity.TaskStatus;
import ec.edu.espe.pa.taskgotchi.repository.HabitRepository;
import ec.edu.espe.pa.taskgotchi.repository.PetEventRepository;
import ec.edu.espe.pa.taskgotchi.repository.PetRepository;
import ec.edu.espe.pa.taskgotchi.repository.TaskRepository;
import ec.edu.espe.pa.taskgotchi.security.CurrentUserProvider;
import ec.edu.espe.pa.taskgotchi.service.DashboardService;
import ec.edu.espe.pa.taskgotchi.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;

import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TaskRepository taskRepository;
    private final HabitRepository habitRepository;
    private final PetEventRepository petEventRepository;
    private final PetRepository petRepository;
    private final PetService petService;
    private final CurrentUserProvider currentUserProvider;
    private final PetMapper petMapper;

    @Override
    @Transactional
    public DashboardSummaryResponse getSummary() {
        Long userId = currentUserProvider.getCurrentUserId();

        long pendingTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.PENDING);
        long overdueTasks = taskRepository.countByUserIdAndDueDateBeforeAndStatus(userId, LocalDateTime.now(), TaskStatus.PENDING);
        long completedTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.COMPLETED);

        int maxStreak = habitRepository.findByUserId(userId).stream()
                .mapToInt(Habit::getCurrentStreak)
                .max()
                .orElse(0);

        PetResponse pet = petService.getPetStatus();

        return new DashboardSummaryResponse(
                pendingTasks,
                overdueTasks,
                completedTasks,
                maxStreak,
                pet
        );
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyStatsResponse getWeeklyStats() {
        Long userId = currentUserProvider.getCurrentUserId();
        LocalDateTime startOfWeek = LocalDateTime.now().with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0);

        List<Task> completedThisWeek = taskRepository.findByUserIdAndStatusAndCompletedAtAfter(userId, TaskStatus.COMPLETED, startOfWeek);
        long habitsCompletedThisWeek = habitRepository.findByUserId(userId).stream()
                .filter(h -> h.getLastCompletedDate() != null && h.getLastCompletedDate().atStartOfDay().isAfter(startOfWeek))
                .count();

        Map<String, Integer> tasksByDay = new HashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            tasksByDay.put(day.name(), 0);
        }

        for (Task task : completedThisWeek) {
            if (task.getCompletedAt() != null) {
                String dayName = task.getCompletedAt().getDayOfWeek().name();
                tasksByDay.put(dayName, tasksByDay.getOrDefault(dayName, 0) + 1);
            }
        }

        return new WeeklyStatsResponse(
                completedThisWeek.size(),
                habitsCompletedThisWeek,
                tasksByDay
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetEventResponse> getPetHistory() {
        Long userId = currentUserProvider.getCurrentUserId();
        Pet pet = petRepository.findByUserId(userId).orElse(null);

        if (pet == null) {
            return List.of();
        }

        return petEventRepository.findTop10ByPetIdOrderByCreatedAtDesc(pet.getId()).stream()
                .map(petMapper::toEventResponse)
                .toList();
    }
}
