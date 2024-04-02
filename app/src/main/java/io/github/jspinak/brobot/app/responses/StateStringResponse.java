package io.github.jspinak.brobot.app.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StateStringResponse {

    private Long id = 0L;
    private String name = "";
    private RegionResponse searchRegion = new RegionResponse();
    private String ownerStateName = "";
    private int timesActedOn = 0;
    private String string = "";

}
