package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.TransitionEntity;
import io.github.jspinak.brobot.app.services.ProjectService;
import io.github.jspinak.brobot.app.web.requests.TransitionCreateRequest;
import io.github.jspinak.brobot.app.web.requests.TransitionUpdateRequest;
import io.github.jspinak.brobot.app.web.responses.TransitionResponse;
import org.springframework.stereotype.Component;

@Component
public class TransitionResponseMapper {

    private final ActionDefinitionResponseMapper actionDefinitionResponseMapper;
    private final ProjectService projectService;

    public TransitionResponseMapper(ActionDefinitionResponseMapper actionDefinitionResponseMapper,
                                    ProjectService projectService) {
        this.actionDefinitionResponseMapper = actionDefinitionResponseMapper;
        this.projectService = projectService;
    }

    public TransitionResponse toResponse(TransitionEntity entity) {
        TransitionResponse response = new TransitionResponse();
        response.setId(entity.getId());
        response.setSourceStateId(entity.getSourceStateId());
        response.setStateImageId(entity.getStateImageId());
        response.setStaysVisibleAfterTransition(entity.getStaysVisibleAfterTransition());
        response.setStatesToEnter(entity.getStatesToEnter());
        response.setStatesToExit(entity.getStatesToExit());
        response.setScore(entity.getScore());
        response.setTimesSuccessful(entity.getTimesSuccessful());
        response.setActionDefinition(actionDefinitionResponseMapper.toResponse(entity.getActionDefinition()));
        return response;
    }

    public TransitionEntity toEntity(TransitionCreateRequest request) {
        TransitionEntity entity = new TransitionEntity();
        updateEntityFromRequest(entity, request);
        return entity;
    }

    public void updateEntityFromRequest(TransitionEntity entity, TransitionCreateRequest request) {
        entity.setProject(projectService.getProjectById(request.getProjectId()));
        entity.setSourceStateId(request.getSourceStateId());
        entity.setStateImageId(request.getStateImageId());
        entity.setStaysVisibleAfterTransition(request.getStaysVisibleAfterTransition());
        entity.setStatesToEnter(request.getStatesToEnter());
        entity.setStatesToExit(request.getStatesToExit());
        entity.setScore(request.getScore());
        entity.setTimesSuccessful(request.getTimesSuccessful());
        entity.setActionDefinition(actionDefinitionResponseMapper.fromRequest(request.getActionDefinition()));
    }

    public void updateEntityFromRequest(TransitionEntity entity, TransitionUpdateRequest request) {
        if (request.getSourceStateId() != null) entity.setSourceStateId(request.getSourceStateId());
        if (request.getStateImageId() != null) entity.setStateImageId(request.getStateImageId());
        if (request.getStaysVisibleAfterTransition() != null) entity.setStaysVisibleAfterTransition(
                request.getStaysVisibleAfterTransition());
        if (request.getStatesToEnter() != null) entity.setStatesToEnter(request.getStatesToEnter());
        if (request.getStatesToExit() != null) entity.setStatesToExit(request.getStatesToExit());
        if (request.getScore() != null) entity.setScore(request.getScore());
        if (request.getTimesSuccessful() != null) entity.setTimesSuccessful(request.getTimesSuccessful());
        if (request.getActionDefinition() != null) entity.setActionDefinition(
                actionDefinitionResponseMapper.fromRequest(request.getActionDefinition()));
    }
}