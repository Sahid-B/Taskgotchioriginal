package ec.edu.espe.pa.taskgotchi.service.impl;

import ec.edu.espe.pa.taskgotchi.dto.HabitLogResponse;
import ec.edu.espe.pa.taskgotchi.dto.HabitRequest;
import ec.edu.espe.pa.taskgotchi.dto.HabitResponse;
import ec.edu.espe.pa.taskgotchi.exception.BusinessRuleException;
import ec.edu.espe.pa.taskgotchi.exception.ResourceNotFoundException;
import ec.edu.espe.pa.taskgotchi.mapper.HabitMapper;
import ec.edu.espe.pa.taskgotchi.entity.Habit;
import ec.edu.espe.pa.taskgotchi.entity.HabitLog;
import ec.edu.espe.pa.taskgotchi.entity.User;
import ec.edu.espe.pa.taskgotchi.repository.HabitLogRepository;
import ec.edu.espe.pa.taskgotchi.repository.HabitRepository;
import ec.edu.espe.pa.taskgotchi.repository.UserRepository;
import ec.edu.espe.pa.taskgotchi.security.CurrentUserProvider;
import ec.edu.espe.pa.taskgotchi.service.HabitService;
import ec.edu.espe.pa.taskgotchi.service.PetInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitServiceImpl implements HabitService {

    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final HabitMapper habitMapper;
    private final PetInternalService petInternalService;

    @Override
    @Transactional(readOnly = true)
    public List<HabitResponse> getAllHabits() {
        Long userId = currentUserProvider.getCurrentUserId();
        return habitRepository.findByUserId(userId).stream()
                .map(habitMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HabitResponse getHabitById(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Habit habit = habitRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito no encontrado o no pertenece al usuario"));
        return habitMapper.toResponse(habit);
    }

    @Override
    @Transactional
    public HabitResponse createHabit(HabitRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Habit habit = habitMapper.toEntity(request);
        habit.setUser(user);
        habit = habitRepository.save(habit);

        return habitMapper.toResponse(habit);
    }

    @Override
    @Transactional
    public HabitResponse updateHabit(Long id, HabitRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        Habit habit = habitRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito no encontrado o no pertenece al usuario"));

        habit.setName(request.name());
        if (request.frequency() != null) {
            habit.setFrequency(request.frequency());
        }
        habit = habitRepository.save(habit);

        return habitMapper.toResponse(habit);
    }

    @Override
    @Transactional
    public void deleteHabit(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Habit habit = habitRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito no encontrado o no pertenece al usuario"));
        habitLogRepository.deleteByHabitId(id);
        habitRepository.delete(habit);
    }

    @Override
    @Transactional
    public HabitResponse checkHabit(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Habit habit = habitRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito no encontrado o no pertenece al usuario"));

        LocalDate today = LocalDate.now();

        if (habitLogRepository.existsByHabitIdAndDate(id, today)) {
            throw new BusinessRuleException("El hábito ya ha sido completado el día de hoy");
        }

        // Registrar HabitLog
        HabitLog habitLog = HabitLog.builder()
                .habit(habit)
                .date(today)
                .build();
        habitLogRepository.save(habitLog);

        // Actualizar racha
        if (habit.getLastCompletedDate() != null && habit.getLastCompletedDate().equals(today.minusDays(1))) {
            habit.setCurrentStreak(habit.getCurrentStreak() + 1);
        } else {
            habit.setCurrentStreak(1);
        }

        if (habit.getCurrentStreak() > habit.getBestStreak()) {
            habit.setBestStreak(habit.getCurrentStreak());
        }

        habit.setLastCompletedDate(today);
        habit = habitRepository.save(habit);

        // Otorgar recompensas a la mascota con bonificación por racha
        int baseBonus = 15;
        int streakBonus = Math.min(50, habit.getCurrentStreak() * 5); // Hasta +50 XP
        int totalXp = baseBonus + streakBonus;
        int totalCoins = 10;

        petInternalService.addXpAndCoins(userId, totalXp, totalCoins, "Completó hábito: " + habit.getName() + " (Racha: " + habit.getCurrentStreak() + " días)");

        return habitMapper.toResponse(habit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitLogResponse> getHabitLogs(Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        Habit habit = habitRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito no encontrado o no pertenece al usuario"));

        return habitLogRepository.findByHabitIdOrderByDateDesc(habit.getId()).stream()
                .map(habitMapper::toLogResponse)
                .toList();
    }
}
