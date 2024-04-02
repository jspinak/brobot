package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class ObjectCollectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "objColl_stateLocations",
            joinColumns = @JoinColumn(name = "objColl_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "stateLocation_id", referencedColumnName = "id"))
    private List<StateLocationEntity> stateLocations = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "objColl_stateImages",
            joinColumns = @JoinColumn(name = "objColl_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "stateImage_id", referencedColumnName = "id"))
    private List<StateImageEntity> stateImages = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "objColl_stateRegions",
            joinColumns = @JoinColumn(name = "objColl_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "stateRegion_id", referencedColumnName = "id"))
    private List<StateRegionEntity> stateRegions = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "objColl_stateStrings",
            joinColumns = @JoinColumn(name = "objColl_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "stateString_id", referencedColumnName = "id"))
    private List<StateStringEntity> stateStrings = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "matches", joinColumns = @JoinColumn(name = "objColl_id"))
    private List<MatchesEntity> matches = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "objColl_scenes",
            joinColumns = @JoinColumn(name = "objColl_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "scene_id", referencedColumnName = "id"))
    private List<PatternEntity> scenes = new ArrayList<>();
}
