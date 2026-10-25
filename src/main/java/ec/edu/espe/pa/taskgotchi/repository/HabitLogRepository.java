package ec.edu.espe.pa.taskgotchi.repository;

import ec.edu.espe.pa.taskgotchi.entity.HabitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {
    List<HabitLog> findByHabitIdOrderByDateDesc(Long habitId);
    boolean existsByHabitIdAndDate(Long habitId, LocalDate date);
    void deleteByHabitId(Long habitId);
}
