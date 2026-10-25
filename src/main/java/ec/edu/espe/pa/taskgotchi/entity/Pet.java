package ec.edu.espe.pa.taskgotchi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PetStage stage;

    @Column(nullable = false)
    private Integer hunger;

    @Column(nullable = false)
    private Integer mood;

    @Column(nullable = false)
    private Integer energy;

    @Column(nullable = false)
    private Integer xp;

    @Column(nullable = false)
    private Integer coins;

    @Column(length = 255)
    private String personality;

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    @PrePersist
    public void prePersist() {
        if (stage == null) stage = PetStage.EGG;
        if (hunger == null) hunger = 80;
        if (mood == null) mood = 80;
        if (energy == null) energy = 80;
        if (xp == null) xp = 0;
        if (coins == null) coins = 0;
        if (personality == null) personality = "Alegre y curioso";
        if (lastUpdatedAt == null) lastUpdatedAt = LocalDateTime.now();
    }
}
