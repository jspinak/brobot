package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.primitives.image.PatternData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface PatternDataRepo extends JpaRepository<PatternData, Long> {
    Optional<PatternData> findByName(String name);
    List<PatternData> findByNameContainingIgnoreCase(String name);

}
