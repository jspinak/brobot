package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.ActionDefinitionEntity;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ActionDefinitionEntityMapper {

    private final ActionStepEntityMapper actionStepEntityMapper;

    public ActionDefinitionEntityMapper(ActionStepEntityMapper actionStepEntityMapper) {
        this.actionStepEntityMapper = actionStepEntityMapper;
    }

    public ActionDefinition mapWithoutImages(ActionDefinitionEntity entity) {
        if (entity == null) return null;

        ActionDefinition actionDefinition = new ActionDefinition();
        actionDefinition.setSteps(actionStepEntityMapper.mapFromEntityListToActionStepListWithoutImages(entity.getSteps()));
        return actionDefinition;
    }

    public ActionDefinitionEntity mapWithoutImages(ActionDefinition actionDefinition) {
        if (actionDefinition == null) return null;

        ActionDefinitionEntity entity = new ActionDefinitionEntity();
        entity.setSteps(actionDefinition.getSteps().stream()
                .map(actionStepEntityMapper::map)
                .collect(Collectors.toList()));
        return entity;
    }
}