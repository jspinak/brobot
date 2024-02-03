package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface PatternRepo extends CrudRepository<Pattern, Long> {
    Optional<Pattern> findByName(String name);
    List<Pattern> findByNameContainingIgnoreCase(String name);

}
