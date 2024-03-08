package com.brobot.app.database.entities;

import com.brobot.app.database.embeddable.PositionEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import jakarta.persistence.*;

@Entity
public class StateLocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name = "";
    @OneToOne(cascade = CascadeType.ALL)
    private LocationEntity location = new LocationEntity();
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

}
