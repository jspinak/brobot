package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface StateImageRepo extends JpaRepository<StateImageEntity, Long> {
    Optional<StateImageEntity> findByName(String name);
    //List<StateImageEntity> findByNameContainingIgnoreCase(String name);
    List<StateImageEntity> findByProjectId(Long projectId);

}
