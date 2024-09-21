package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.ActionOptionsEntityMapper;
import io.github.jspinak.brobot.app.database.databaseMappers.ObjectCollectionEntityMapper;
import io.github.jspinak.brobot.app.database.entities.ActionDefinitionEntity;
import io.github.jspinak.brobot.app.database.entities.ActionStepEntity;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import io.github.jspinak.brobot.dsl.ActionStep;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActionDefinitionService {


    private final ActionOptionsEntityMapper actionOptionsEntityMapper;
    private final ObjectCollectionEntityMapper objectCollectionEntityMapper;
    private final ActionStepService actionStepService;

    public ActionDefinitionService(ActionOptionsEntityMapper actionOptionsEntityMapper,
                                   ObjectCollectionEntityMapper objectCollectionEntityMapper,
                                   ActionStepService actionStepService) {
        this.actionOptionsEntityMapper = actionOptionsEntityMapper;
        this.objectCollectionEntityMapper = objectCollectionEntityMapper;
        this.actionStepService = actionStepService;
    }

    public ActionDefinitionEntity createActionDefinitionEntity(ActionDefinition actionDefinition) {
        ActionDefinitionEntity entity = new ActionDefinitionEntity();

        List<ActionStepEntity> steps = actionDefinition.getSteps().stream()
                .map(this::createActionStepEntity)
                .collect(Collectors.toList());

        entity.setSteps(steps);

        return entity;
    }

    private ActionStepEntity createActionStepEntity(ActionStep step) {
        ActionStepEntity entity = new ActionStepEntity();
        entity.setActionOptionsEntity(actionOptionsEntityMapper.map(step.getOptions()));
        entity.setObjectCollectionEntity(objectCollectionEntityMapper.map(step.getObjects()));
        return entity;
    }

    public ActionDefinition mapFromEntityToLibraryClass(ActionDefinitionEntity actionDefinitionEntity) {
        ActionDefinition actionDefinition = new ActionDefinition();
        actionDefinition.setSteps(actionStepService.mapFromEntitiesToActionSteps(actionDefinitionEntity.getSteps()));
        return actionDefinition;
    }

}
