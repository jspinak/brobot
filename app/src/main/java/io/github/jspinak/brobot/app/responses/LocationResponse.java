package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import lombok.Getter;

@Getter
public class LocationResponse {

    private String name = "";
    private int x = 0;
    private int y = 0;
    private RegionResponse region = new RegionResponse();
    private PositionResponse position = new PositionResponse();
    private Positions.Name anchor = Positions.Name.MIDDLEMIDDLE;

}
