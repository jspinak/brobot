package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.SceneEntity;
import io.github.jspinak.brobot.app.services.PatternService;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SceneEntityMapper {

    public SceneEntity map(Scene scene, PatternService patternService) {
        SceneEntity sceneEntity = new SceneEntity();
        sceneEntity.setPattern(patternService.map(scene.getPattern()));
        return sceneEntity;
    }

    public Scene map(SceneEntity sceneEntity, PatternService patternService) {
        return new Scene(patternService.map(sceneEntity.getPattern()));
    }

    public List<Scene> mapToSceneList(List<SceneEntity> sceneEntities, PatternService patternService) {
        List<Scene> scenes = new ArrayList<>();
        sceneEntities.forEach(sceneEntity -> scenes.add(map(sceneEntity, patternService)));
        return scenes;
    }

    public List<SceneEntity> mapToSceneEntityList(List<Scene> scenes, PatternService patternService) {
        List<SceneEntity> sceneEntitiesList = new ArrayList<>();
        scenes.forEach(scene -> sceneEntitiesList.add(map(scene, patternService)));
        return sceneEntitiesList;
    }
}
