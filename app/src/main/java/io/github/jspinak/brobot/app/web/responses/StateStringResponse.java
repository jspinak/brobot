package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class StateStringResponse {

    private StateObject.Type objectType;
    private String name;
    private RegionResponse searchRegion;
    private String ownerStateName;
    private int timesActedOn;
    private String string;
}
