package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.entities.StateRegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface StateRegionRepo extends JpaRepository<StateRegionEntity, Long> {
    Optional<StateRegionEntity> findByName(String name);
    //List<StateRegionEntity> findByNameContainingIgnoreCase(String name);

}
