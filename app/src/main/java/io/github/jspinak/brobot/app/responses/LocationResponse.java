package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationResponse {

    private String name = "";
    private int x = 0;
    private int y = 0;
    private RegionResponse region = new RegionResponse();
    private Position position = new Position();
    private Positions.Name anchor = Positions.Name.MIDDLEMIDDLE;

}
