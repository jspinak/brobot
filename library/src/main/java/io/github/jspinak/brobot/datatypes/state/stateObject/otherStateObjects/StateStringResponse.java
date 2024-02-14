package io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.region.RegionResponse;
import lombok.Getter;

@Getter
public class StateStringResponse {

    private Long id = 0L;
    private String name = "";
    private RegionResponse searchRegion = new RegionResponse(new Region());
    private String ownerStateName = "";
    private int timesActedOn = 0;
    private String string = "";

    public StateStringResponse(StateString stateString) {
        if (stateString == null) return;
        id = stateString.getId();
        name = stateString.getName();
        searchRegion = new RegionResponse(stateString.getSearchRegion());
        ownerStateName = stateString.getOwnerStateName();
        timesActedOn = stateString.getTimesActedOn();
        string = stateString.getString();
    }
}
