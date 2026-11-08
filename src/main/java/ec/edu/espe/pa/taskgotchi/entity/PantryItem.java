package ec.edu.espe.pa.taskgotchi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pantry_items", uniqueConstraints = @UniqueConstraint(columnNames = {"pet_id", "food_type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PantryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(name = "food_type", nullable = false, length = 30)
    private String foodType;

    @Column(nullable = false)
    private int quantity;
}
