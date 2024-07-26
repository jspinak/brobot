package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class StateRegionResponse {

    private StateObject.Type objectType;
    private String name;
    private RegionResponse searchRegion;
    private String ownerStateName;
    private int staysVisibleAfterClicked;
    private int probabilityExists;
    private int timesActedOn;
    private PositionResponse position;
    private AnchorsResponse anchors;
    private String mockText;
    private MatchHistoryResponse matchHistory;
}
