package io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects;

import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistoryResponse;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.region.RegionResponse;
import lombok.Getter;

@Getter
public class StateRegionResponse {

    private Long id = 0L;
    private String name = "";
    private RegionResponse searchRegion = new RegionResponse(new Region());
    private String ownerStateName = "";
    private int staysVisibleAfterClicked = 0;
    private int probabilityExists = 100;
    private int timesActedOn = 0;
    private Position position = new Position();
    private Anchors anchors = new Anchors();
    private MatchHistoryResponse matchHistory = new MatchHistoryResponse(new MatchHistory());
    private String mockText = "";

    public StateRegionResponse(StateRegion stateRegion) {
        if (stateRegion == null) return;
        id = stateRegion.getId();
        name = stateRegion.getName();
        searchRegion = new RegionResponse(stateRegion.getSearchRegion());
        ownerStateName = stateRegion.getOwnerStateName();
        staysVisibleAfterClicked = stateRegion.getStaysVisibleAfterClicked();
        probabilityExists = stateRegion.getProbabilityExists();
        timesActedOn = stateRegion.getTimesActedOn();
        position = stateRegion.getPosition(); //new PositionResponse(stateRegion.getPosition());
        anchors = stateRegion.getAnchors();
        mockText = stateRegion.getMockText();
        matchHistory = new MatchHistoryResponse(stateRegion.getMatchHistory());
    }
}
