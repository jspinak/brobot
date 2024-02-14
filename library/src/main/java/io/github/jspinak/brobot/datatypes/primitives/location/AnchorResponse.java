package io.github.jspinak.brobot.datatypes.primitives.location;

import lombok.Getter;

@Getter
public class AnchorResponse {

    private Positions.Name anchorInNewDefinedRegion;
    private Position positionInMatch;

    public AnchorResponse(Anchor anchor) {
        anchorInNewDefinedRegion = anchor.getAnchorInNewDefinedRegion();
        positionInMatch = anchor.getPositionInMatch();
    }

}
