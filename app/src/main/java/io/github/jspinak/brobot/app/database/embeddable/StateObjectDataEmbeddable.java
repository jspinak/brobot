package io.github.jspinak.brobot.app.database.embeddable;

import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class StateObjectDataEmbeddable {

    private String stateObjectId;
    private StateObject.Type objectType;
    private String stateObjectName;
    private String ownerStateName;

}
