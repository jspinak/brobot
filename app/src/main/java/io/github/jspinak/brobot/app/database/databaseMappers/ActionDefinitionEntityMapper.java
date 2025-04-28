package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.ActionDefinitionEntity;
import io.github.jspinak.brobot.app.services.PatternService;
import io.github.jspinak.brobot.app.services.SceneService;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import io.github.jspinak.brobot.dsl.ActionStep;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ActionDefinitionEntityMapper {

    private final ActionStepEntityMapper actionStepEntityMapper;

    public ActionDefinitionEntityMapper(ActionStepEntityMapper actionStepEntityMapper) {
        this.actionStepEntityMapper = actionStepEntityMapper;
    }

    public ActionDefinition map(ActionDefinitionEntity entity, SceneService sceneService, PatternService patternService) {
        if (entity == null) return null;

        ActionDefinition actionDefinition = new ActionDefinition();
        List<ActionStep> actionSteps = actionStepEntityMapper.map(entity.getSteps(), sceneService, patternService);
        actionDefinition.setSteps(actionSteps);
        return actionDefinition;
    }

    public ActionDefinitionEntity map(ActionDefinition actionDefinition, SceneService sceneService, PatternService patternService) {
        if (actionDefinition == null) return null;

        ActionDefinitionEntity entity = new ActionDefinitionEntity();
        entity.setSteps(actionDefinition.getSteps().stream()
                .map(step -> actionStepEntityMapper.map(step, sceneService, patternService))
                .collect(Collectors.toList()));
        return entity;
    }
}