package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.state.state.State;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface StateRepo extends CrudRepository<State, Long> {
    List<State> findAllAsList();
    Optional<State> findByName(String name);
    List<State> findByNameContainingIgnoreCase(String name);

}
