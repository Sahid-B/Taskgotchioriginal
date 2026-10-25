package ec.edu.espe.pa.taskgotchi.repository;

import ec.edu.espe.pa.taskgotchi.model.Boss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BossRepository extends JpaRepository<Boss, Long> {
    List<Boss> findByUserId(Long userId);
    Optional<Boss> findByIdAndUserId(Long id, Long userId);
}
