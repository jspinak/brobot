package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StateObjectDataRequest {
    private String stateObjectId;
    private String objectType;
    private String stateObjectName;
    private String ownerStateName;
}
