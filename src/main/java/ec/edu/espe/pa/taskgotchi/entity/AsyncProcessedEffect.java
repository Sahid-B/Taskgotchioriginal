package ec.edu.espe.pa.taskgotchi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "async_processed_effects", uniqueConstraints = {
        @UniqueConstraint(name = "uk_async_effect_event_type", columnNames = {"event_id", "effect_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsyncProcessedEffect {

    public enum Status { PROCESSING, COMPLETED, FAILED, DEAD }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "effect_type", nullable = false, length = 60)
    private String effectType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false)
    private int attempts;

    @Column(length = 1000)
    private String lastError;

    @Column(length = 120)
    private String payloadType;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String payloadJson;

    private Long userId;

    @Column(length = 100)
    private String requestId;

    private LocalDateTime nextAttemptAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
