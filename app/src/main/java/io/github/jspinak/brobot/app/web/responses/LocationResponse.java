package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class LocationResponse {
    private Long id;
    private String name;
    private boolean definedByXY;
    private int locX;
    private int locY;
    private RegionResponse region;
    private PositionResponse position;
    private String anchor; // assuming Positions.Name is an enum
}

