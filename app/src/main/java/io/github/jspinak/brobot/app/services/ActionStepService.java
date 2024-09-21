package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.database.databaseMappers.ActionOptionsEntityMapper;
import io.github.jspinak.brobot.app.database.databaseMappers.ObjectCollectionEntityMapper;
import io.github.jspinak.brobot.app.database.entities.ActionOptionsEntity;
import io.github.jspinak.brobot.app.database.entities.ActionStepEntity;
import io.github.jspinak.brobot.app.database.entities.ObjectCollectionEntity;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.dsl.ActionStep;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ActionStepService {

    private final ActionOptionsEntityMapper actionOptionsEntityMapper;
    private final ObjectCollectionEntityMapper objectCollectionEntityMapper;
    private final StateImageService stateImageService;
    private final ObjectCollectionService objectCollectionService;

    public ActionStepService(ActionOptionsEntityMapper actionOptionsEntityMapper,
                             ObjectCollectionEntityMapper objectCollectionEntityMapper,
                             StateImageService stateImageService,
                             ObjectCollectionService objectCollectionService) {
        this.actionOptionsEntityMapper = actionOptionsEntityMapper;
        this.objectCollectionEntityMapper = objectCollectionEntityMapper;
        this.stateImageService = stateImageService;
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
}
