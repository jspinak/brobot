package com.brobot.app.restControllers;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.database.services.SceneService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scenes")
public class SceneController {

    private final SceneService sceneService;

    public SceneController(SceneService sceneService) {
        this.sceneService = sceneService;
    }

    public @ResponseBody List<Scene> getAllScenes() {
        return sceneService.getAllScenes();
    }

    public @ResponseBody Scene getScene(String name) {
        return sceneService.getScene(name);
    }
}
