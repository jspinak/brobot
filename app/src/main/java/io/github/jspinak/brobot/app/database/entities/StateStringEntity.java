package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.app.database.embeddable.RegionEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import jakarta.persistence.*;

@Entity
public class StateStringEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name = "";
    @Embedded
    private RegionEmbeddable searchRegion = new RegionEmbeddable();
    private String ownerStateName = "";
    private int timesActedOn = 0;
    private String string = "";

}
