package ec.edu.espe.pa.taskgotchi.repository;

import ec.edu.espe.pa.taskgotchi.entity.Task;
import ec.edu.espe.pa.taskgotchi.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    Optional<Task> findByIdAndUserId(Long id, Long userId);
    List<Task> findByUserId(Long userId);
    List<Task> findByUserIdAndBossId(Long userId, Long bossId);
    List<Task> findByBossId(Long bossId);
    long countByUserIdAndStatus(Long userId, TaskStatus status);
    long countByUserIdAndDueDateBeforeAndStatus(Long userId, LocalDateTime dateTime, TaskStatus status);
    List<Task> findByUserIdAndStatusAndCompletedAtAfter(Long userId, TaskStatus status, LocalDateTime dateTime);

    boolean existsByCodeAndUserId(String code, Long userId);
    boolean existsByCodeAndUserIdAndIdNot(String code, Long userId, Long id);
}
