package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface SceneRepo extends CrudRepository<Scene, Long> {
    Optional<Scene> findByName(String name);

}
