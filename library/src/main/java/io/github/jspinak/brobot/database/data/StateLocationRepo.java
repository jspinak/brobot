package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface StateLocationRepo extends CrudRepository<StateLocation, Long> {
    Optional<StateLocation> findByName(String name);
    List<StateLocation> findByNameContainingIgnoreCase(String name);

}
