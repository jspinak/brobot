package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateText;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface StateTextRepo extends CrudRepository<StateText, Long> {
    List<StateText> findAllAsList();
    Optional<StateText> findByName(String name);
    List<StateText> findByNameContainingIgnoreCase(String name);

}
