package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.app.database.embeddable.PositionEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
public class StateLocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private StateObject.Type objectType = StateObject.Type.LOCATION;
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
