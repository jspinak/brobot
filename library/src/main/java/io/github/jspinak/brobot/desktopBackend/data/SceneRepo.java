package io.github.jspinak.brobot.desktopBackend.data;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface SceneRepo extends CrudRepository<Scene, Long> {
    List<Scene> findAll();
    Optional<Scene> findByName(String name);
    List<Scene> findByNameContainingAndIgnoreCase(String name);

}
