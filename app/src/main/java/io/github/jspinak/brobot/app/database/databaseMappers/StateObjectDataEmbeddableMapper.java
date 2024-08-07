package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.embeddable.StateObjectDataEmbeddable;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;
import org.springframework.stereotype.Component;

@Component
public class StateObjectDataEmbeddableMapper {

    public StateObjectDataEmbeddable map(StateObjectData stateObjectData) {
        StateObjectDataEmbeddable stateObjectDataEmbeddable = new StateObjectDataEmbeddable();
        stateObjectDataEmbeddable.setObjectType(stateObjectData.getObjectType());
        stateObjectDataEmbeddable.setStateObjectId(stateObjectData.getStateObjectId());
        stateObjectDataEmbeddable.setStateObjectName(stateObjectData.getStateObjectName());
        stateObjectDataEmbeddable.setOwnerStateName(stateObjectData.getOwnerStateName());
        return stateObjectDataEmbeddable;
    }

    public StateObjectData map(StateObjectDataEmbeddable stateObjectDataEmbeddable) {
        StateObjectData stateObjectData = new StateObjectData();
        stateObjectData.setObjectType(stateObjectDataEmbeddable.getObjectType());
        stateObjectData.setStateObjectId(stateObjectDataEmbeddable.getStateObjectId());
        stateObjectData.setStateObjectName(stateObjectDataEmbeddable.getStateObjectName());
        stateObjectData.setOwnerStateName(stateObjectDataEmbeddable.getOwnerStateName());
        return stateObjectData;
    }
}
