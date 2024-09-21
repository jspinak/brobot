package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.database.entities.ActionOptionsEntity;
import io.github.jspinak.brobot.app.database.entities.ActionStepEntity;
import io.github.jspinak.brobot.app.database.entities.ObjectCollectionEntity;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.dsl.ActionStep;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ActionStepEntityMapper {
    private final ActionOptionsEntityMapper actionOptionsMapper;
    private final ObjectCollectionEntityMapper objectCollectionMapper;

    public ActionStepEntityMapper(ActionOptionsEntityMapper actionOptionsMapper,
                                  ObjectCollectionEntityMapper objectCollectionMapper) {
        this.actionOptionsMapper = actionOptionsMapper;
        this.objectCollectionMapper = objectCollectionMapper;
    }

    public ActionStepEntity map(ActionStep step) {
        if (step == null) return null;

        ActionStepEntity entity = new ActionStepEntity();
        entity.setActionOptionsEntity(actionOptionsMapper.map(step.getOptions()));
        entity.setObjectCollectionEntity(objectCollectionMapper.map(step.getObjects()));
        return entity;
    }

    public ActionStep mapFromEntityToActionStepWithoutImages(ActionStepEntity actionStepEntity) {
        if (actionStepEntity == null) return null;
        ActionOptions actionOptions = mapOptions(actionStepEntity.getActionOptionsEntity());
        ObjectCollection objectCollection = mapObjectCollectionWithoutImages(actionStepEntity.getObjectCollectionEntity());
        return new ActionStep(actionOptions, objectCollection);
    }

    public List<ActionStep> mapFromEntityListToActionStepListWithoutImages(List<ActionStepEntity> actionStepEntities) {
        List<ActionStep> actionSteps = new ArrayList<>();
        actionStepEntities.forEach(actionStepEntity -> actionSteps.add(mapFromEntityToActionStepWithoutImages(actionStepEntity)));
        return actionSteps;
    }

    public ActionOptions mapOptions(ActionOptionsEntity entity) {
        return actionOptionsMapper.map(entity);
    }

    public ObjectCollection mapObjectCollectionWithoutImages(ObjectCollectionEntity objectCollectionEntity) {
        return objectCollectionMapper.mapWithoutStateImagesAndScenes(objectCollectionEntity);
    }

}
