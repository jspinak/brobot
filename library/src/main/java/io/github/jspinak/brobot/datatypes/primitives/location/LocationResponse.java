package io.github.jspinak.brobot.datatypes.primitives.location;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.region.RegionResponse;
import lombok.Getter;

@Getter
public class LocationResponse {

    private Long id = 0L;
    private String name = "";
    private int x = 0;
    private int y = 0;
    private RegionResponse region = new RegionResponse(new Region());
    private Position position = new Position();
    private Positions.Name anchor = Positions.Name.MIDDLEMIDDLE;

    public LocationResponse(Location location) {
        if (location == null) return;
        id = location.getId();
        name = location.getName();
        x = location.getX();
        y = location.getY();
        location.getRegion().ifPresent(reg -> region = new RegionResponse(reg));
        position = location.getPosition();
        anchor = location.getAnchor();
    }
}
