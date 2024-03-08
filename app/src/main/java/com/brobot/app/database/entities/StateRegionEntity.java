package com.brobot.app.database.entities;

import com.brobot.app.database.embeddable.PositionEmbeddable;
import com.brobot.app.database.embeddable.RegionEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import jakarta.persistence.*;

@Entity
public class StateRegionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name = "";
    @Embedded
    private RegionEmbeddable searchRegion = new RegionEmbeddable();
    private String ownerStateName = "";
    private int staysVisibleAfterClicked = 0;
    private int probabilityExists = 100;
    private int timesActedOn = 0;
    @Embedded
    private PositionEmbeddable position = new PositionEmbeddable();
    @OneToOne(cascade = CascadeType.ALL)
    private AnchorsEntity anchors = new AnchorsEntity();
    @OneToOne(cascade = CascadeType.ALL)
    private MatchHistoryEntity matchHistory = new MatchHistoryEntity();
    private String mockText = "";

}
