package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.embeddable.StateObjectDataEmbeddable;
import io.github.jspinak.brobot.app.web.requests.StateObjectDataRequest;
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

    public StateObjectDataEmbeddable fromRequest(StateObjectDataRequest request) {
        if (request == null) return null;
        StateObjectDataEmbeddable entity = new StateObjectDataEmbeddable();
        entity.setStateObjectId(request.getStateObjectId());

        // Convert String to Positions.Name enum
        if (request.getObjectType() != null) {
            try {
                entity.setObjectType(StateObject.Type.valueOf(request.getObjectType()));
            } catch (IllegalArgumentException e) {
                // Handle the case where the string doesn't match any enum constant
                // You might want to log this error or throw a custom exception
                throw new IllegalArgumentException("Invalid anchor value: " + request.getObjectType(), e);
            }
        }

        entity.setStateObjectName(request.getStateObjectName());
        entity.setOwnerStateName(request.getOwnerStateName());
        return entity;
    }

    public StateObjectDataRequest toRequest(StateObjectDataEmbeddable embeddable) {
        if (embeddable == null) {
            return null;
        }

        StateObjectDataRequest request = new StateObjectDataRequest();
        request.setStateObjectId(embeddable.getStateObjectId());
        request.setObjectType(embeddable.getObjectType() != null ? embeddable.getObjectType().name() : null);
        request.setStateObjectName(embeddable.getStateObjectName());
        request.setOwnerStateName(embeddable.getOwnerStateName());

        return request;
    }
}
