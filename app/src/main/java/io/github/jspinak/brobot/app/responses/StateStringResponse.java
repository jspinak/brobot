package io.github.jspinak.brobot.app.responses;

import lombok.Getter;

@Getter
public class StateStringResponse {

    private Long id = 0L;
    private String name = "";
    private RegionResponse searchRegion = new RegionResponse();
    private String ownerStateName = "";
    private int timesActedOn = 0;
    private String string = "";

}
