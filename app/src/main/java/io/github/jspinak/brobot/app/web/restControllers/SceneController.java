package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.database.entities.SceneEntity;
import io.github.jspinak.brobot.app.services.SceneService;
import io.github.jspinak.brobot.app.web.responseMappers.SceneResponseMapper;
import io.github.jspinak.brobot.app.web.responses.SceneResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/scenes")
@CrossOrigin(origins = "http://localhost:3000")
public class SceneController {

    private final SceneService sceneService;
    private final SceneResponseMapper sceneResponseMapper;

    public SceneController(SceneService sceneService, SceneResponseMapper sceneResponseMapper) {
        this.sceneService = sceneService;
        this.sceneResponseMapper = sceneResponseMapper;
    }

    @GetMapping("/all") // Maps to GET /api/scenes/all
    public List<SceneResponse> getAllScenes() {
        return sceneService.getSceneResponses();
    }

    @GetMapping("/name/{name}") // Maps to GET /api/scenes/name/{name}
    public List<SceneResponse> getSceneByName(@PathVariable String name) {
        return sceneService.getScenesByName(name);
    }

    @GetMapping("/{id}") // maps to GET /api/scenes/{id}
    public SceneResponse getScene(@PathVariable Integer id) {
        Optional<SceneEntity> sceneOpt = sceneService.getSceneById((long)id);
        return sceneOpt.map(sceneResponseMapper::map).orElse(null);
    }
}
