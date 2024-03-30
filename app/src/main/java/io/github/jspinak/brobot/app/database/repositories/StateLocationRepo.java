package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.entities.StateLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface StateLocationRepo extends JpaRepository<StateLocationEntity, Long> {
    Optional<StateLocationEntity> findByName(String name);
    //List<StateLocationEntity> findByNameContainingIgnoreCase(String name);

}
