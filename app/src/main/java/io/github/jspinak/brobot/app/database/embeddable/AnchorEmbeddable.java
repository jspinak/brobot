package io.github.jspinak.brobot.app.database.embeddable;

import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Data;

@Embeddable
@Data
public class AnchorEmbeddable {

    private Positions.Name anchorInNewDefinedRegion;
    @Embedded
    private PositionEmbeddable positionInMatch;
}
