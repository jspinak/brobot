package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.app.database.embeddable.PositionEmbeddable;
import io.github.jspinak.brobot.app.database.embeddable.RegionEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class LocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private boolean definedByXY = true;
    private int locX = -1;
    private int locY = -1;
    @Embedded
    private RegionEmbeddable region;
    @Embedded
    private PositionEmbeddable position;
    private Positions.Name anchor;

}
