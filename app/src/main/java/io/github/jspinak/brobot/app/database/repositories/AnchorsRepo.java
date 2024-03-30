package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.entities.AnchorsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface AnchorsRepo extends JpaRepository<AnchorsEntity, Long> {
    List<AnchorsEntity> findAll();
    Optional<AnchorsEntity> findById(Long id);

}
