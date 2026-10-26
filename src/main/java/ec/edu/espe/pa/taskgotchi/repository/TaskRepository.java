package ec.edu.espe.pa.taskgotchi.repository;

import ec.edu.espe.pa.taskgotchi.entity.Task;
import ec.edu.espe.pa.taskgotchi.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    Optional<Task> findByIdAndUserId(Long id, Long userId);
    Page<Task> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title, Pageable pageable);
    List<Task> findByUserId(Long userId);
    List<Task> findByUserIdAndBossId(Long userId, Long bossId);
    List<Task> findByBossId(Long bossId);
    long countByUserIdAndStatus(Long userId, TaskStatus status);
    List<Task> findByUserIdAndStatusAndCompletedAtAfter(Long userId, TaskStatus status, LocalDateTime dateTime);

    boolean existsByCodeAndUserId(String code, Long userId);
    boolean existsByCodeAndUserIdAndIdNot(String code, Long userId, Long id);

    @Modifying
    @Query("""
            update Task t
               set t.status = :overdueStatus,
                   t.updatedAt = :now
             where t.status = :pendingStatus
               and t.dueDate is not null
               and t.dueDate < :now
            """)
    int markOverdueTasks(@Param("pendingStatus") TaskStatus pendingStatus,
                         @Param("overdueStatus") TaskStatus overdueStatus,
                         @Param("now") LocalDateTime now);
}
