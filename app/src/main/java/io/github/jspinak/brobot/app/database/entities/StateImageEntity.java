package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
public class StateImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId = 0L;
    private StateObject.Type objectType = StateObject.Type.IMAGE;
    private String name = "";

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "stateImage_patterns",
            joinColumns = @JoinColumn(name = "stateImage_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "pattern_id", referencedColumnName = "id"))
    private List<PatternEntity> patterns = new ArrayList<>();

    private Long ownerStateId = -1L;
    private String ownerStateName = "";
    private int timesActedOn = 0;
    private boolean shared = false;
    //private KmeansProfilesAllSchemas kmeansProfilesAllSchemas;
    //private ColorCluster colorCluster;
    private int index = 0;
    private boolean dynamic = false;

    @ElementCollection
    @CollectionTable(name = "state_image_involved_transitions",
            joinColumns = @JoinColumn(name = "state_image_id"))
    @Column(name = "transition_id")
    private Set<Long> involvedTransitionIds = new HashSet<>();
}
