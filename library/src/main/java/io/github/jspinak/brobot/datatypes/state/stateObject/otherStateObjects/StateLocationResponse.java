package io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects;

import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.LocationResponse;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistoryResponse;
import lombok.Getter;

@Getter
public class StateLocationResponse {

    private Long id = 0L;
    private String name = "";
    private LocationResponse location = new LocationResponse(new Location());
    private String ownerStateName = "";
    private int staysVisibleAfterClicked = 0;
    private int probabilityExists = 100;
    private int timesActedOn = 0;
    private Position position = new Position();
    private Anchors anchors = new Anchors();
    private MatchHistoryResponse matchHistory = new MatchHistoryResponse(new MatchHistory());

    public StateLocationResponse(StateLocation stateLocation) {
        if (stateLocation == null) return;
        id = stateLocation.getId();
        name = stateLocation.getName();
        location = new LocationResponse(stateLocation.getLocation());
        ownerStateName = stateLocation.getOwnerStateName();
        staysVisibleAfterClicked = stateLocation.getStaysVisibleAfterClicked();
        probabilityExists = stateLocation.getProbabilityExists();
        timesActedOn = stateLocation.getTimesActedOn();
        position = stateLocation.getPosition();
        anchors = stateLocation.getAnchors();
        matchHistory = new MatchHistoryResponse(stateLocation.getMatchHistory());
    }

}
