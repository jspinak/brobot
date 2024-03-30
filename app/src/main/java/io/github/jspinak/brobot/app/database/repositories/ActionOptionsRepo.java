package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.entities.ActionOptionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ActionOptionsRepo extends JpaRepository<ActionOptionsEntity, Long> {

}
