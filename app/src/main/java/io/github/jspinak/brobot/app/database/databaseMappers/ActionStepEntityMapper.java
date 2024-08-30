package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.database.entities.ActionOptionsEntity;
import io.github.jspinak.brobot.app.database.entities.ActionStepEntity;
import io.github.jspinak.brobot.app.database.entities.ObjectCollectionEntity;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import org.springframework.stereotype.Component;

@Component
public class ActionStepEntityMapper {
    private final ActionOptionsEntityMapper actionOptionsMapper;
    private final ObjectCollectionEntityMapper objectCollectionMapper;

    public ActionStepEntityMapper(ActionOptionsEntityMapper actionOptionsMapper, ObjectCollectionEntityMapper objectCollectionMapper) {
        this.actionOptionsMapper = actionOptionsMapper;
        this.objectCollectionMapper = objectCollectionMapper;
    }

    public ActionStepEntity map(ActionDefinition.ActionStep step) {
        if (step == null) return null;

        ActionStepEntity entity = new ActionStepEntity();
        entity.setActionOptions(actionOptionsMapper.map(step.getOptions()));
        entity.setObjectCollection(objectCollectionMapper.map(step.getObjects()));
        return entity;
    }

    public ActionOptions mapOptions(ActionOptionsEntity entity) {
        return actionOptionsMapper.map(entity);
    }

    public ObjectCollection mapObjectCollection(ObjectCollectionEntity entity) {
        return objectCollectionMapper.map(entity);
    }
}
