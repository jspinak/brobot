package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface PatternRepo extends CrudRepository<Pattern, Long> {

    List<Pattern> findAllAsList();
    Optional<Pattern> findByName(String name);
    List<Pattern> findByNameContainingIgnoreCase(String name);

}
