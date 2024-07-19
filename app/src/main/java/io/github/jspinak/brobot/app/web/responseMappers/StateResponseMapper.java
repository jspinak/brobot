package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.web.responses.StateResponse;
import org.springframework.stereotype.Component;

@Component
public class StateResponseMapper {

    private final StateImageResponseMapper stateImageResponseMapper;

    public StateResponseMapper(StateImageResponseMapper stateImageResponseMapper) {
        this.stateImageResponseMapper = stateImageResponseMapper;
    }

    public StateResponse map(StateEntity stateEntity) {
        StateResponse stateResponse = new StateResponse();
        stateResponse.setId(stateEntity.getId());
        stateResponse.setName(stateEntity.getName());
        stateEntity.getStateImages().forEach(image ->
                stateResponse.getStateImages().add(stateImageResponseMapper.map(image)));
        return stateResponse;
    }

}
