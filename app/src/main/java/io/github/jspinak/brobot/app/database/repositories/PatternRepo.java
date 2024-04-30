package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface PatternRepo extends JpaRepository<PatternEntity, Long> {
    List<PatternEntity> findByName(String name);
    @Query("SELECT p FROM PatternEntity p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<PatternEntity> findByPatternDataNameContainingIgnoreCase(@Param("name") String name);

}
