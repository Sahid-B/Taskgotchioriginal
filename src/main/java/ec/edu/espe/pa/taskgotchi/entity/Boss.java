package ec.edu.espe.pa.taskgotchi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bosses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Boss {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private Integer maxHp;

    @Column(nullable = false)
    private Integer currentHp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BossStatus status;

    @Column(length = 255)
    private String rewards;

    @PrePersist
    public void prePersist() {
        if (status == null) status = BossStatus.ACTIVE;
        if (currentHp == null) currentHp = maxHp;
    }
}
