package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.database.databaseMappers.ActionOptionsEntityMapper;
import io.github.jspinak.brobot.app.database.entities.ActionStepEntity;
import io.github.jspinak.brobot.app.web.requests.ActionStepRequest;
import io.github.jspinak.brobot.app.web.responseMappers.ActionOptionsResponseMapper;
import io.github.jspinak.brobot.app.web.responseMappers.ObjectCollectionResponseMapper;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.dsl.ActionStep;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ActionStepService {

    private final ActionOptionsEntityMapper actionOptionsEntityMapper;
    private final ActionOptionsResponseMapper actionOptionsResponseMapper;
    private final ObjectCollectionResponseMapper objectCollectionResponseMapper;
    private final ObjectCollectionService objectCollectionService;

    public ActionStepService(ActionOptionsEntityMapper actionOptionsEntityMapper,
                             ActionOptionsResponseMapper actionOptionsResponseMapper,
                             ObjectCollectionResponseMapper objectCollectionResponseMapper,
                             ObjectCollectionService objectCollectionService) {
        this.actionOptionsEntityMapper = actionOptionsEntityMapper;
        this.actionOptionsResponseMapper = actionOptionsResponseMapper;
        this.objectCollectionResponseMapper = objectCollectionResponseMapper;
        this.objectCollectionService = objectCollectionService;
    }

    public ActionStep mapFromEntityToActionStep(ActionStepEntity actionStepEntity) {
        ActionOptions actionOptions = actionOptionsEntityMapper.map(actionStepEntity.getActionOptionsEntity());
        ObjectCollection objectCollection = objectCollectionService.mapObjectCollection(actionStepEntity.getObjectCollectionEntity());
        return new ActionStep(actionOptions, objectCollection);
    }

    public List<ActionStep> mapFromEntitiesToActionSteps(List<ActionStepEntity> stepEntites) {
        List<ActionStep> steps = new ArrayList<>();
        stepEntites.forEach(entity -> steps.add(mapFromEntityToActionStep(entity)));
        return steps;
    }

    public ActionStepEntity createActionStepEntity(ActionStepRequest actionStepRequest) {
        ActionStepEntity entity = new ActionStepEntity();
        entity.setActionOptionsEntity(actionOptionsResponseMapper.fromRequest(actionStepRequest.getActionOptions()));
        entity.setObjectCollectionEntity(objectCollectionResponseMapper.fromRequest(actionStepRequest.getObjectCollection()));
        return entity;
    }
}
