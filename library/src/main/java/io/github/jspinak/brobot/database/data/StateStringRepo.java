package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface StateStringRepo extends CrudRepository<StateString, Long> {
    List<StateString> findAllAsList();
    Optional<StateString> findByName(String name);
    List<StateString> findByNameContainingIgnoreCase(String name);

}
