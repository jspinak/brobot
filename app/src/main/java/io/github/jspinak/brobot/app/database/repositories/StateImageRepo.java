package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface StateImageRepo extends JpaRepository<StateImageEntity, Long> {
    Optional<StateImageEntity> findByName(String name);
    List<StateImageEntity> findByProjectId(Long projectId);
    List<StateImageEntity> findByOwnerStateId(Long stateId);
    void deleteByOwnerStateId(Long stateId);

    @Query("SELECT DISTINCT s.projectId FROM StateImageEntity s")
    List<Long> findDistinctProjectIds();

    // Add a direct native query to bypass any potential JPA issues
    @Query(value = "SELECT * FROM state_image_entity WHERE project_id = :projectId",
            nativeQuery = true)
    List<StateImageEntity> findByProjectIdNative(@Param("projectId") Long projectId);
}
