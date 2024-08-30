package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.entities.StateTransitionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StateTransitionsRepo extends JpaRepository<StateTransitionsEntity, Long> {
    Optional<StateTransitionsEntity> findByStateId(Long stateId);
}
