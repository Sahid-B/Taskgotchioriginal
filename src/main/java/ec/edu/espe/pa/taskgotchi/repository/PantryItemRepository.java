package ec.edu.espe.pa.taskgotchi.repository;

import ec.edu.espe.pa.taskgotchi.entity.PantryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PantryItemRepository extends JpaRepository<PantryItem, Long> {
    List<PantryItem> findByPetId(Long petId);
    Optional<PantryItem> findByPetIdAndFoodType(Long petId, String foodType);
}
