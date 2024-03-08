package com.brobot.app.responses;

import lombok.Getter;

@Getter
public class StateLocationResponse {

    private String name = "";
    private LocationResponse location = new LocationResponse();
    private String ownerStateName = "";
    private int staysVisibleAfterClicked = 0;
    private int probabilityExists = 100;
    private int timesActedOn = 0;
    private PositionResponse position = new PositionResponse();
    private AnchorsResponse anchors = new AnchorsResponse();
    private MatchHistoryResponse matchHistory = new MatchHistoryResponse();

}
