package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.entities.StateStringEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface StateStringRepo extends JpaRepository<StateStringEntity, Long> {
    Optional<StateStringEntity> findByName(String name);
    //List<StateStringEntity> findByNameContainingIgnoreCase(String name);

}
