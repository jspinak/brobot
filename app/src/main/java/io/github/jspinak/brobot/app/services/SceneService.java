package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.SceneEntityMapper;
import io.github.jspinak.brobot.app.database.entities.SceneEntity;
import io.github.jspinak.brobot.app.database.repositories.SceneRepo;
import io.github.jspinak.brobot.app.web.responseMappers.ImageResponseMapper;
import io.github.jspinak.brobot.app.web.responseMappers.SceneResponseMapper;
import io.github.jspinak.brobot.app.web.responses.SceneResponse;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SceneService {

    private final SceneRepo sceneRepo;
    private final SceneEntityMapper sceneEntityMapper;
    private final SceneResponseMapper sceneResponseMapper;
    private final ImageResponseMapper imageResponseMapper;

    public SceneService(SceneRepo sceneRepo, SceneEntityMapper sceneEntityMapper,
                        SceneResponseMapper sceneResponseMapper,
                        ImageResponseMapper imageResponseMapper) {
        this.sceneRepo = sceneRepo;
        this.sceneEntityMapper = sceneEntityMapper;
        this.sceneResponseMapper = sceneResponseMapper;
        this.imageResponseMapper = imageResponseMapper;
    }

    public List<SceneEntity> getSceneEntities() {
        return sceneRepo.findAll();
    }

    public List<SceneResponse> getSceneResponses() {
        List<SceneEntity> sceneEntities = getSceneEntities();
        List<SceneResponse> sceneResponses = new ArrayList<>();
        for (SceneEntity sceneEntity : sceneEntities) {
            sceneResponses.add(sceneResponseMapper.map(sceneEntity));
        }
        return sceneResponses;
    }

    public List<SceneResponse> getScenesByName(String name) {
        List<SceneEntity> sceneEntities = getSceneEntities();
        List<SceneResponse> sceneResponses = new ArrayList<>();
        for (SceneEntity sceneEntity : sceneEntities) {
            if (sceneEntity.getPattern().getName().equals(name)) {
                sceneResponses.add(sceneResponseMapper.map(sceneEntity));
            }
        }
        return sceneResponses;
    }

    public Optional<SceneEntity> getSceneById(Long id) {
        return sceneRepo.findById(id);
    }

    public SceneEntity saveScene(Scene scene) {
        SceneEntity sceneEntity = sceneEntityMapper.map(scene);
        sceneRepo.save(sceneEntity);
        scene.setId(sceneEntity.getId());
        return sceneEntity;
    }

    public void saveScenes(List<Scene> scenes) {
        scenes.forEach(this::saveScene);
    }

    public void saveScene(SceneResponse sceneResponse) {
        SceneEntity sceneEntity = sceneResponseMapper.map(sceneResponse);
        sceneRepo.save(sceneEntity);
    }

    public SceneEntity getOrCreateScene(Scene scene) {
        Optional<SceneEntity> sceneEntity = getSceneById(scene.getId());
        return sceneEntity.orElseGet(() -> saveScene(scene));
    }

    public List<SceneEntity> mapToSceneEntityList(List<Scene> scenes) {
        List<SceneEntity> sceneEntities = new ArrayList<>();
        for (Scene scene : scenes) {
            Optional<SceneEntity> sceneEntity = getSceneById(scene.getId());
            sceneEntity.ifPresent(sceneEntities::add);
        }
        return sceneEntities;
    }
}
