package io.github.jspinak.brobot.desktopBackend.api;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.desktopBackend.data.SceneRepo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SceneService {

    private SceneRepo sceneRepo;

    public SceneService(SceneRepo sceneRepo) {
        this.sceneRepo = sceneRepo;
    }

    public Scene getScene(String name) {
        return sceneRepo.findByName(name).orElse(null);
    }

    public List<Scene> getAllScenes() {
        return sceneRepo.findAll();
    }

    public void saveScene(Scene scene) {
        sceneRepo.save(scene);
    }

}
