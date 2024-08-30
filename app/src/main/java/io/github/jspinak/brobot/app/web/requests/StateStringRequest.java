package io.github.jspinak.brobot.app.web.requests;

import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StateStringRequest {
    private StateObject.Type objectType;
    private String name;
    private RegionRequest searchRegion;
    private String ownerStateName;
    private int timesActedOn;
    private String string;
}
