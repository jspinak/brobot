package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.ActionDefinitionEntity;
import io.github.jspinak.brobot.app.web.requests.ActionDefinitionRequest;
import io.github.jspinak.brobot.app.web.responses.ActionDefinitionResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ActionDefinitionResponseMapper {

    private final ActionStepResponseMapper actionStepResponseMapper;

    public ActionDefinitionResponseMapper(ActionStepResponseMapper actionStepResponseMapper) {
        this.actionStepResponseMapper = actionStepResponseMapper;
    }

    public ActionDefinitionResponse toResponse(ActionDefinitionEntity entity) {
        if (entity == null) {
            return null;
        }

        ActionDefinitionResponse response = new ActionDefinitionResponse();
        response.setId(entity.getId());
        response.setActionType(entity.getActionType());
        response.setSteps(entity.getSteps().stream()
                .map(actionStepResponseMapper::toResponse)
                .collect(Collectors.toList()));

        return response;
    }

    public ActionDefinitionEntity toEntity(ActionDefinitionResponse response) {
        if (response == null) {
            return null;
        }

        ActionDefinitionEntity entity = new ActionDefinitionEntity();
        entity.setId(response.getId());
        entity.setActionType(response.getActionType());
        entity.setSteps(response.getSteps().stream()
                .map(actionStepResponseMapper::toEntity)
                .collect(Collectors.toList()));

        return entity;
    }

    public ActionDefinitionEntity fromRequest(ActionDefinitionRequest request) {
        if (request == null) {
            return null;
        }

        ActionDefinitionEntity entity = new ActionDefinitionEntity();
        entity.setSteps(request.getSteps().stream()
                .map(actionStepResponseMapper::fromRequest)
                .collect(Collectors.toList()));

        return entity;
    }

}