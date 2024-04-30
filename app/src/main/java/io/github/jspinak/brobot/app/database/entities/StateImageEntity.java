package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class StateImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long projectId = 0L;
    private StateObject.Type objectType = StateObject.Type.IMAGE;
    private String name = "";
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "stateImage_patterns",
            joinColumns = @JoinColumn(name = "stateImage_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "pattern_id", referencedColumnName = "id"))
    private List<PatternEntity> patterns = new ArrayList<>();
    private String ownerStateName = "";
    private int timesActedOn = 0;
    private boolean shared = false;
    //private KmeansProfilesAllSchemas kmeansProfilesAllSchemas;
    //private ColorCluster colorCluster;
    private int index = 0;
    private boolean dynamic = false;
}
