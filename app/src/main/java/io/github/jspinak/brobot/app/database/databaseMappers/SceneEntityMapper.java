package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.SceneEntity;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SceneEntityMapper {


    private final PatternEntityMapper patternEntityMapper;

    public SceneEntityMapper(PatternEntityMapper patternEntityMapper) {
        this.patternEntityMapper = patternEntityMapper;
    }

    public SceneEntity map(Scene scene) {
        SceneEntity sceneEntity = new SceneEntity();
        sceneEntity.setPattern(patternEntityMapper.map(scene.getPattern()));
        return sceneEntity;
    }

    public Scene map(SceneEntity sceneEntity) {
        return new Scene(patternEntityMapper.map(sceneEntity.getPattern()));
    }

    public List<Scene> mapToSceneList(List<SceneEntity> sceneEntities) {
        List<Scene> scenes = new ArrayList<>();
        sceneEntities.forEach(sceneEntity -> scenes.add(map(sceneEntity)));
        return scenes;
    }

    public List<SceneEntity> mapToSceneEntityList(List<Scene> sceneEntities) {
        List<SceneEntity> sceneEntitiesList = new ArrayList<>();
        sceneEntities.forEach(sceneEntity -> sceneEntitiesList.add(map(sceneEntity)));
        return sceneEntitiesList;
    }
}
