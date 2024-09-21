package io.github.jspinak.brobot.app.database.entities;

import io.github.jspinak.brobot.app.database.embeddable.RegionEmbeddable;
import io.github.jspinak.brobot.app.database.embeddable.StateObjectDataEmbeddable;
import jakarta.persistence.*;
import lombok.Data;
import org.bytedeco.opencv.opencv_core.Mat;

import java.time.LocalDateTime;

@Entity
@Data
public class MatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private StateObjectDataEmbeddable stateObjectData;
    @Transient
    private Mat histogram;
    private Long sceneId; // Scene here would create a circular reference
    private LocalDateTime timeStamp = LocalDateTime.now();
    private int timesActedOn = 0;
}
