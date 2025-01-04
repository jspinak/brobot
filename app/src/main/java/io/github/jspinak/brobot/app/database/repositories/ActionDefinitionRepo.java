package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.entities.ActionDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionDefinitionRepo extends JpaRepository<ActionDefinitionEntity, Long> {

    List<ActionDefinitionEntity> findByProjectId(Long projectId);
}
