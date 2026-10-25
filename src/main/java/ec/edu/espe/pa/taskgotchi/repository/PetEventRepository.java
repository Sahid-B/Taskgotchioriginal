package ec.edu.espe.pa.taskgotchi.repository;

import ec.edu.espe.pa.taskgotchi.entity.PetEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetEventRepository extends JpaRepository<PetEvent, Long> {
    Page<PetEvent> findByPetIdOrderByCreatedAtDesc(Long petId, Pageable pageable);
    List<PetEvent> findTop10ByPetIdOrderByCreatedAtDesc(Long petId);
}
