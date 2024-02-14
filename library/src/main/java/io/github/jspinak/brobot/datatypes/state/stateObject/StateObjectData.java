package io.github.jspinak.brobot.datatypes.state.stateObject;

import jakarta.persistence.Embeddable;
import lombok.Getter;

/**
 * Provides the essential data fields to store a StateObject in other variables, like Match.
 * Storing the entire StateObject would lead to circular object chains, which is a suboptimal architecture and
 * causes problems with persistence (StateObject is an @Entity whereas lower level fields are mostly @Embedded).
 * StateObject implementations such as StateImage have their own repositories and individual StateObjects can be found in the
 * respective repository using the data in this class.
 */
@Embeddable
@Getter
public class StateObjectData {

    private final Long stateObjectId;
    private final StateObject.Type objectType;
    private final String stateObjectName;
    private final String ownerStateName;

    public StateObjectData(StateObject stateObject) {
        this.stateObjectId = stateObject.getId();
        this.objectType = stateObject.getObjectType();
        this.stateObjectName = stateObject.getName();
        this.ownerStateName = stateObject.getOwnerStateName();
    }

    public StateObjectData() {
        stateObjectId = 0L;
        objectType = StateObject.Type.IMAGE;
        stateObjectName = "";
        ownerStateName = "";
    }

    @Override
    public String toString() {
        return "StateObject: " + stateObjectName + ", " + objectType + ", ownerState=" + ownerStateName + ", id=" + stateObjectId;
    }

}
