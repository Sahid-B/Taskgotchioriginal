package ec.edu.espe.pa.taskgotchi.repository;

import ec.edu.espe.pa.taskgotchi.entity.AsyncProcessedEffect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AsyncProcessedEffectRepository extends JpaRepository<AsyncProcessedEffect, Long> {
    Optional<AsyncProcessedEffect> findByEventIdAndEffectType(UUID eventId, String effectType);
    List<AsyncProcessedEffect> findTop50ByStatusAndNextAttemptAtBeforeOrderByNextAttemptAtAsc(
            AsyncProcessedEffect.Status status, LocalDateTime now);
}
