package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.app.database.embeddable.PositionEmbeddable;
import io.github.jspinak.brobot.app.database.embeddable.SearchRegionsEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PatternEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String url; // originally type URL, which requires conversion for use with JPA
    private String imgpath;
    private String name;
    private boolean fixed = false;
    @Embedded
    private SearchRegionsEmbeddable searchRegions = new SearchRegionsEmbeddable();
    private boolean setKmeansColorProfiles = false;
    //@Embedded
    //private KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
    //@Embedded
    //private ColorCluster colorCluster = new ColorCluster();
    @OneToOne(cascade = CascadeType.ALL)
    private MatchHistoryEntity matchHistory = new MatchHistoryEntity();
    private int index;
    private boolean dynamic = false;
    @Embedded
    private PositionEmbeddable position = new PositionEmbeddable();
    @OneToOne(cascade = CascadeType.ALL)
    private AnchorsEntity anchors = new AnchorsEntity();
    @OneToOne(cascade = CascadeType.ALL)
    private ImageEntity image;

}
