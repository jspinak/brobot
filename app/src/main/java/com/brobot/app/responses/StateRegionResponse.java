package com.brobot.app.responses;

import lombok.Getter;

@Getter
public class StateRegionResponse {

    private String name = "";
    private RegionResponse searchRegion = new RegionResponse();
    private String ownerStateName = "";
    private int staysVisibleAfterClicked = 0;
    private int probabilityExists = 100;
    private int timesActedOn = 0;
    private PositionResponse position = new PositionResponse();
    private AnchorsResponse anchors = new AnchorsResponse();
    private MatchHistoryResponse matchHistory = new MatchHistoryResponse();
    private String mockText = "";
}
