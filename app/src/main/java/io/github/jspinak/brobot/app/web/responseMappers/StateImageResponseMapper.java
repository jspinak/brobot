package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.app.web.responses.StateImageResponse;
import org.springframework.stereotype.Component;

@Component
public class StateImageResponseMapper {

    private final PatternResponseMapper patternResponseMapper;

    public StateImageResponseMapper(PatternResponseMapper patternResponseMapper) {
        this.patternResponseMapper = patternResponseMapper;
    }

    public StateImageResponse map(StateImageEntity stateImageEntity) {
        StateImageResponse stateImageResponse = new StateImageResponse();
        stateImageResponse.setId(stateImageEntity.getId());
        stateImageResponse.setName(stateImageEntity.getName());
        stateImageEntity.getPatterns().forEach(pattern ->
                stateImageResponse.getPatterns().add(patternResponseMapper.map(pattern)));
        return stateImageResponse;
    }

}
