package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationRequest {
    private Long id;
    private String name;
    private boolean definedByXY;
    private int locX;
    private int locY;
    private RegionRequest region;
    private PositionRequest position;
    private String anchor; // Positions.Name is an enum
}
