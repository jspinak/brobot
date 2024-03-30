package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface StateRepo extends JpaRepository<StateEntity, Long> {
    Optional<StateEntity> findByName(String name);
    //@Query("SELECT s FROM StateEntity s WHERE UPPER(s.name) LIKE UPPER(:name)")
    //List<StateEntity> findByNameIgnoreCase(String name);
    List<StateEntity> findByProjectId(Long projectId);

}
