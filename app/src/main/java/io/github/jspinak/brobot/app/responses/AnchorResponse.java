package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnchorResponse {

    private Positions.Name anchorInNewDefinedRegion;
    private Position positionInMatch;

}
