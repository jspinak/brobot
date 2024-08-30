package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.SceneEntity;
import io.github.jspinak.brobot.app.web.requests.SceneRequest;
import io.github.jspinak.brobot.app.web.responses.SceneResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SceneResponseMapper {

    private final PatternResponseMapper patternResponseMapper;

    public SceneResponseMapper(PatternResponseMapper patternResponseMapper) {
        this.patternResponseMapper = patternResponseMapper;
    }

    public SceneEntity map(SceneResponse sceneResponse) {
        SceneEntity sceneEntity = new SceneEntity();
        sceneEntity.setId(sceneResponse.getId());
        sceneEntity.setPattern(patternResponseMapper.map(sceneResponse.getPattern()));
        return sceneEntity;
    }

    public SceneResponse map(SceneEntity sceneEntity) {
        SceneResponse sceneResponse = new SceneResponse();
        sceneResponse.setId(sceneEntity.getId());
        sceneResponse.setPattern(patternResponseMapper.map(sceneEntity.getPattern()));
        return sceneResponse;
    }

    public List<SceneResponse> mapToSceneResponseList(List<SceneEntity> scenes) {
        List<SceneResponse> sceneResponses = new ArrayList<>();
        for (SceneEntity sceneEntity : scenes) {
            sceneResponses.add(map(sceneEntity));
        }
        return sceneResponses;
    }

    public List<SceneEntity> mapToSceneEntityList(List<SceneResponse> sceneResponses) {
        List<SceneEntity> sceneEntities = new ArrayList<>();
        for (SceneResponse sceneResponse : sceneResponses) {
            sceneEntities.add(map(sceneResponse));
        }
        return sceneEntities;
    }

    public SceneEntity fromRequest(SceneRequest request) {
        if (request == null) return null;
        SceneEntity entity = new SceneEntity();
        entity.setId(request.getId());
        entity.setPattern(patternResponseMapper.fromRequest(request.getPattern()));
        return entity;
    }

    public SceneRequest toRequest(SceneEntity entity) {
        if (entity == null) {
            return null;
        }

        SceneRequest request = new SceneRequest();
        request.setId(entity.getId());
        request.setPattern(patternResponseMapper.toRequest(entity.getPattern()));

        return request;
    }
}
