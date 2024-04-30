package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.app.database.embeddable.RegionEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class StateStringEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private StateObject.Type objectType = StateObject.Type.STRING;
    private String name = "";
    @Embedded
    private RegionEmbeddable searchRegion = new RegionEmbeddable();
    private String ownerStateName = "";
    private int timesActedOn = 0;
    private String string = "";

}
