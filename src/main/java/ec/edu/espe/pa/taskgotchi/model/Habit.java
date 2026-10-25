package ec.edu.espe.pa.taskgotchi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "habits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String frequency;

    @Column(nullable = false)
    private Integer currentStreak;

    @Column(nullable = false)
    private Integer bestStreak;

    private LocalDate lastCompletedDate;

    @Column(nullable = false)
    private Boolean active;

    @PrePersist
    public void prePersist() {
        if (currentStreak == null) currentStreak = 0;
        if (bestStreak == null) bestStreak = 0;
        if (active == null) active = true;
        if (frequency == null) frequency = "DAILY";
    }
}
