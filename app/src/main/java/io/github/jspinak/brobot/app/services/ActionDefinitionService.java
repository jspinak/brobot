package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.ActionOptionsEntityMapper;
import io.github.jspinak.brobot.app.database.databaseMappers.ObjectCollectionEntityMapper;
import io.github.jspinak.brobot.app.database.entities.ActionDefinitionEntity;
import io.github.jspinak.brobot.app.database.entities.ActionStepEntity;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActionDefinitionService {


    private final ActionOptionsEntityMapper actionOptionsEntityMapper;
    private final ObjectCollectionEntityMapper objectCollectionEntityMapper;

    public ActionDefinitionService(ActionOptionsEntityMapper actionOptionsEntityMapper,
                                   ObjectCollectionEntityMapper objectCollectionEntityMapper) {
        this.actionOptionsEntityMapper = actionOptionsEntityMapper;
        this.objectCollectionEntityMapper = objectCollectionEntityMapper;
    }

    public ActionDefinitionEntity createActionDefinitionEntity(ActionDefinition actionDefinition) {
        ActionDefinitionEntity entity = new ActionDefinitionEntity();

        List<ActionStepEntity> steps = actionDefinition.getSteps().stream()
                .map(this::createActionStepEntity)
                .collect(Collectors.toList());

        entity.setSteps(steps);

        return entity;
    }

    private ActionStepEntity createActionStepEntity(ActionDefinition.ActionStep step) {
        ActionStepEntity entity = new ActionStepEntity();
        entity.setActionOptions(actionOptionsEntityMapper.map(step.getOptions()));
        entity.setObjectCollection(objectCollectionEntityMapper.map(step.getObjects()));
        return entity;
    }
}
