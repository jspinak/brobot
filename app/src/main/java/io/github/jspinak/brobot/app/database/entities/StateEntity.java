package io.github.jspinak.brobot.app.database.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jspinak.brobot.app.database.embeddable.RegionEmbeddable;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Data
public class StateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project", nullable = false)
    @JsonIgnore // Avoids serialization of ProjectEntity in StateEntity
    private ProjectEntity project = new ProjectEntity();

    private String name = "";
    @ElementCollection
    @CollectionTable(name = "state_stateText", joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"))
    private Set<String> stateText = new HashSet<>();
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "state_stateImages",
            joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "stateImage_id", referencedColumnName = "id"))
    private Set<StateImageEntity> stateImages = new HashSet<>();
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "state_stateStrings",
            joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "stateString_id", referencedColumnName = "id"))
    private Set<StateStringEntity> stateStrings = new HashSet<>();
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "state_stateRegions",
            joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "stateRegion_id", referencedColumnName = "id"))
    private Set<StateRegionEntity> stateRegions = new HashSet<>();
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "state_stateLocations",
            joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "stateLocation_id", referencedColumnName = "id"))
    private Set<StateLocationEntity> stateLocations = new HashSet<>();
    private boolean blocking = false;
    @ElementCollection
    @CollectionTable(name = "state_canHide", joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"))
    private Set<String> canHide = new HashSet<>();
    @ElementCollection
    @CollectionTable(name = "state_canHide_ids", joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"))
    private Set<Long> canHideIds = new HashSet<>();
    @ElementCollection
    @CollectionTable(name = "state_hidden", joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"))
    private Set<String> hidden = new HashSet<>();
    @ElementCollection
    @CollectionTable(name = "state_hidden_ids", joinColumns = @JoinColumn(name = "state_id", referencedColumnName = "id"))
    private Set<Long> hiddenStateIds = new HashSet<>();
    private int pathScore = 1;
    private LocalDateTime lastAccessed = LocalDateTime.now();
    private int baseProbabilityExists = 100;
    private int probabilityExists = 0;
    private int timesVisited = 0;
    @ManyToMany
    @JoinTable(name = "state_scenes",
            joinColumns = @JoinColumn(name = "state_id"),
            inverseJoinColumns = @JoinColumn(name = "scene_id"))
    private List<SceneEntity> scenes = new ArrayList<>();
    @Embedded
    private RegionEmbeddable usableArea = new RegionEmbeddable();
    @OneToOne(cascade = CascadeType.ALL)
    private MatchHistoryEntity matchHistory = new MatchHistoryEntity();

    public StateEntity() {}

    // Constructor with ID (for mapping from library)
    public StateEntity(Long id) {
        this();
        this.id = id;
    }

    @Transient
    @JsonProperty("projectId")
    public Long getProjectId() {
        return project != null ? project.getId() : null;
    }

    // Remove the project from toString, equals, and hashCode methods
    @Override
    public String toString() {
        return "StateEntity(id=" + id + ", name=" + name + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StateEntity)) return false;
        StateEntity that = (StateEntity) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
