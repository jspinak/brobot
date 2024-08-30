package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.StateTransitionsEntity;
import io.github.jspinak.brobot.app.web.responses.StateTransitionsResponse;
import org.springframework.stereotype.Component;

@Component
public class StateTransitionsResponseMapper {

    private final TransitionResponseMapper transitionResponseMapper;

    public StateTransitionsResponseMapper(TransitionResponseMapper transitionResponseMapper) {
        this.transitionResponseMapper = transitionResponseMapper;
    }

    public StateTransitionsResponse map(StateTransitionsEntity entity) {
        StateTransitionsResponse response = new StateTransitionsResponse();
        response.setId(entity.getId());
        response.setStateId(entity.getStateId());
        response.setTransitions(entity.getTransitions().stream()
                .map(transitionResponseMapper::toResponse)
                .toList());
        if (entity.getFinishTransition() != null) {
            response.setFinishTransition(transitionResponseMapper.toResponse(entity.getFinishTransition()));
        }
        return response;
    }
}
