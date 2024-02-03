package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface StateRegionRepo extends CrudRepository<StateRegion, Long> {
    Optional<StateRegion> findByName(String name);
    List<StateRegion> findByNameContainingIgnoreCase(String name);

}
