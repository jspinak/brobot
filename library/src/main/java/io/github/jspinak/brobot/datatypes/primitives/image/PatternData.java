package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * The non-image fields of Pattern. Used with Pattern and PatternResponse.
 */
@Entity
@Getter
@Setter
public class PatternData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // fields from SikuliX Pattern
    private String url; // originally type URL, which requires conversion for use with JPA
    private String imgpath;

    private String name;
    /*
    An image that should always appear in the same location has fixed==true.
    */
    private boolean fixed = false;
    @OneToOne(cascade = CascadeType.ALL)
    private SearchRegions searchRegions = new SearchRegions();
    private boolean setKmeansColorProfiles = false; // this is an expensive operation and should be done only when needed
    //@Embedded
    //private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
    //@Embedded
    //private ColorCluster colorCluster = new ColorCluster();
    @OneToOne(cascade = CascadeType.ALL)
    private MatchHistory matchHistory = new MatchHistory();
    private int index; // a unique identifier used for classification matrices
    private boolean dynamic = false; // dynamic images cannot be found using pattern matching
    @Embedded
    private Position position = new Position(.5,.5); // use to convert a match to a location
    @OneToOne(cascade = CascadeType.ALL)
    private Anchors anchors = new Anchors(); // for defining regions using this object as input
}
