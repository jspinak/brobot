package io.github.jspinak.brobot.app.web.requests;

import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StateLocationRequest {
    private StateObject.Type objectType;
    private String name;
    private LocationRequest location;
    private String ownerStateName;
    private int staysVisibleAfterClicked;
    private int probabilityExists;
    private int timesActedOn;
    private PositionRequest position;
    private AnchorsRequest anchors;
    private MatchHistoryRequest matchHistory;
}
