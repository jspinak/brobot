package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.illustratedHistory.StateIllustration;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
public class StateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long projectId = 0L;
    private String name = "";
    @ElementCollection
    @CollectionTable(name = "state_stateText", joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"))
    private Set<String> stateText = new HashSet<>();
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "state_stateImages",
            joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "stateImage_id", referencedColumnName = "id"))
    private Set<StateImageEntity> stateImages = new HashSet<>();
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "state_stateStrings",
            joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "stateString_id", referencedColumnName = "id"))
    private Set<StateStringEntity> stateStrings = new HashSet<>();
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "state_stateRegions",
            joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "stateRegion_id", referencedColumnName = "id"))
    private Set<StateRegionEntity> stateRegions = new HashSet<>();
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "state_stateLocations",
            joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "stateLocation_id", referencedColumnName = "id"))
    private Set<StateLocationEntity> stateLocations = new HashSet<>();
    private boolean blocking = false;
    @ElementCollection
    @CollectionTable(name = "state_canHide", joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"))
    private Set<String> canHide = new HashSet<>();
    @ElementCollection
    @CollectionTable(name = "state_hidden", joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"))
    private Set<String> hidden = new HashSet<>();
    private int pathScore = 1;
    private LocalDateTime lastAccessed = LocalDateTime.now();
    private int baseProbabilityExists = 100;
    private int probabilityExists = 0;
    private int timesVisited = 0;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "state_scenes",
            joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "scene_id", referencedColumnName = "id"))
    private List<ImageEntity> scenes = new ArrayList<>();
    //@OneToMany(cascade = CascadeType.ALL)
    //@JoinTable(name = "state_illustrastions",
    //        joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"),
    //        inverseJoinColumns = @JoinColumn(name = "illustration_id", referencedColumnName = "id"))
    @Transient
    private List<StateIllustration> illustrations = new ArrayList<>();
    @OneToOne(cascade = CascadeType.ALL)
    private MatchHistoryEntity matchHistory = new MatchHistoryEntity();
}
