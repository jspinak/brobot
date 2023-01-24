package io.github.jspinak.brobot.desktopBackend.data;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface StateImageObjectRepo extends CrudRepository<StateImageObject, Long> {
    List<StateImageObject> findAll();
    Optional<StateImageObject> findByName(String name);
    List<StateImageObject> findByNameContainingIgnoreCase(String name);

}
