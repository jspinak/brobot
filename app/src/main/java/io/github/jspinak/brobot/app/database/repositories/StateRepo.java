package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StateRepo extends JpaRepository<StateEntity, Long> {
    Optional<StateEntity> findByName(String name);

    @Query("SELECT s FROM StateEntity s WHERE s.project.id = :projectId")
    List<StateEntity> findByProjectId(Long projectId);

    @Query("SELECT DISTINCT s.project.id FROM StateEntity s")
    List<Long> findDistinctProjectIds();

    @Query("SELECT COALESCE(MAX(s.project.id), 0) FROM StateEntity s")
    Long findMaxProjectId();
}