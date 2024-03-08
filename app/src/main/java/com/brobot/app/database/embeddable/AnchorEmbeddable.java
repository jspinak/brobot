package com.brobot.app.database.embeddable;

import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class AnchorEmbeddable {

    private Positions.Name anchorInNewDefinedRegion;
    @Embedded
    private PositionEmbeddable positionInMatch;
}
