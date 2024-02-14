package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface PatternRepo extends JpaRepository<Pattern, Long> {
    // Define custom query method to find patterns by name in PatternData
    List<Pattern> findByPatternDataName(String name);
    @Query("SELECT p FROM Pattern p WHERE LOWER(p.patternData.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Pattern> findByPatternDataNameContainingIgnoreCase(@Param("name") String name);

}
