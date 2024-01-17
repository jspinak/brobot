package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface StateLocationRepo extends CrudRepository<StateLocation, Long> {
    List<StateLocation> findAllAsList();
    Optional<StateLocation> findByName(String name);
    List<StateLocation> findByNameContainingIgnoreCase(String name);

}
