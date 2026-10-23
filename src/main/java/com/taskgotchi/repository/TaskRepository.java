package com.taskgotchi.repository;

import com.taskgotchi.model.Task;
import com.taskgotchi.model.TaskStatus;
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
}
