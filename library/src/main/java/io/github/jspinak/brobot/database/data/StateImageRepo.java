package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface StateImageRepo extends CrudRepository<StateImage, Long> {
    List<StateImage> findAllAsList();
    Optional<StateImage> findByName(String name);
    List<StateImage> findByNameContainingIgnoreCase(String name);

}
