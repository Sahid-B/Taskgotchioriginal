package ec.edu.espe.pa.taskgotchi.repository;

import ec.edu.espe.pa.taskgotchi.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    Optional<Pet> findByUserId(Long userId);
    Optional<Pet> findByIdAndUserId(Long id, Long userId);
}
