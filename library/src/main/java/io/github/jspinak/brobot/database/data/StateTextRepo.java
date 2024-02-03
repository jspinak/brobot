package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateText;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface StateTextRepo extends CrudRepository<StateText, Long> {
    Optional<StateText> findByName(String name);
    List<StateText> findByNameContainingIgnoreCase(String name);

}
