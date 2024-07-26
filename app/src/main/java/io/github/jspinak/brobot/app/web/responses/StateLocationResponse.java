package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class StateLocationResponse {

    private StateObject.Type objectType;
    private String name;
    private LocationResponse location;
    private String ownerStateName;
    private int staysVisibleAfterClicked;
    private int probabilityExists;
    private int timesActedOn;
    private PositionResponse position;
    private AnchorsResponse anchors;
    private MatchHistoryResponse matchHistory;
}
