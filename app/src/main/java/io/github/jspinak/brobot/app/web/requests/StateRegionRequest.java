package io.github.jspinak.brobot.app.web.requests;

import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StateRegionRequest {
    private StateObject.Type objectType;
    private String name;
    private RegionRequest searchRegion;
    private String ownerStateName;
    private int staysVisibleAfterClicked;
    private int probabilityExists;
    private int timesActedOn;
    private PositionRequest position;
    private AnchorsRequest anchors;
    private String mockText;
    private MatchHistoryRequest matchHistory;
}
