package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import lombok.Getter;

@Getter
public class AnchorResponse {

    private Positions.Name anchorInNewDefinedRegion;
    private PositionResponse positionInMatch;

}
