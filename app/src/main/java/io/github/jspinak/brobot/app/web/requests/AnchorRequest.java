package io.github.jspinak.brobot.app.web.requests;

import io.github.jspinak.brobot.app.database.embeddable.PositionEmbeddable;

public class AnchorRequest {

    private String anchorInNewDefinedRegion; // Positions.Name is an enum
    private PositionEmbeddable positionInMatch;

}
