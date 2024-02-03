package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.state.state.State;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface StateRepo extends CrudRepository<State, Long> {
    Optional<State> findByNameAsString(String name);
    List<State> findByNameAsStringContainingIgnoreCase(String name);

}
