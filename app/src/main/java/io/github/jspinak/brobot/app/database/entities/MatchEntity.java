package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.app.database.embeddable.RegionEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;
import jakarta.persistence.*;
import lombok.Data;
import org.bytedeco.opencv.opencv_core.Mat;
import java.time.LocalDateTime;

@Entity
@Data
public class MatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private double score = 0.0;
    @OneToOne(cascade = CascadeType.ALL)
    private LocationEntity target;
    @OneToOne(cascade = CascadeType.ALL)
    private ImageEntity image;
    private String text = "";
    private String name = "";
    @Embedded
    private RegionEmbeddable region;
    @OneToOne(cascade = CascadeType.ALL)
    private ImageEntity searchImage;
    @OneToOne(cascade = CascadeType.ALL)
    private AnchorsEntity anchors;
    @Embedded
    private StateObjectData stateObjectData;
    @Transient
    private Mat histogram;
    @OneToOne(cascade = CascadeType.ALL)
    private ImageEntity scene;
    private LocalDateTime timeStamp;
    private int timesActedOn = 0;
}
