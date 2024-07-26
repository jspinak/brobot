package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.embeddable.StateObjectDataEmbeddable;
import io.github.jspinak.brobot.app.web.responses.StateObjectDataResponse;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import org.springframework.stereotype.Component;

@Component
public class StateObjectDataResponseMapper {

    public StateObjectDataResponse map(StateObjectDataEmbeddable stateObjectDataEmbeddable) {
        if (stateObjectDataEmbeddable == null) {
            return null;
        }
        StateObjectDataResponse stateObjectDataResponse = new StateObjectDataResponse();
        stateObjectDataResponse.setStateObjectId(stateObjectDataEmbeddable.getStateObjectId());
        stateObjectDataResponse.setObjectType(stateObjectDataEmbeddable.getObjectType().name());
        stateObjectDataResponse.setStateObjectName(stateObjectDataEmbeddable.getStateObjectName());
        stateObjectDataResponse.setOwnerStateName(stateObjectDataEmbeddable.getOwnerStateName());
        return stateObjectDataResponse;
    }

    public StateObjectDataEmbeddable map(StateObjectDataResponse stateObjectDataResponse) {
        if (stateObjectDataResponse == null) {
            return null;
        }
        StateObjectDataEmbeddable stateObjectDataEmbeddable = new StateObjectDataEmbeddable();
        stateObjectDataEmbeddable.setStateObjectId(stateObjectDataResponse.getStateObjectId());
        stateObjectDataEmbeddable.setObjectType(StateObject.Type.valueOf(stateObjectDataResponse.getObjectType()));
        stateObjectDataEmbeddable.setStateObjectName(stateObjectDataResponse.getStateObjectName());
        stateObjectDataEmbeddable.setOwnerStateName(stateObjectDataResponse.getOwnerStateName());
        return stateObjectDataEmbeddable;
    }
}
