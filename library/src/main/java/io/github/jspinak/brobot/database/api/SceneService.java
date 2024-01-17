package io.github.jspinak.brobot.database.api;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.database.data.SceneRepo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SceneService {

    private final SceneRepo sceneRepo;

    public SceneService(SceneRepo sceneRepo) {
        this.sceneRepo = sceneRepo;
    }

    public Scene getScene(String name) {
        return sceneRepo.findByName(name).orElse(null);
    }

    public List<Scene> getAllScenes() {
        return sceneRepo.findAllAsList();
    }

    public void saveScene(Scene scene) {
        sceneRepo.save(scene);
    }

}
