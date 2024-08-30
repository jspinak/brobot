package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MatchRequest {
    private Long id;
    private double score;
    private LocationRequest target;
    private ImageRequest image;
    private String text;
    private String name;
    private RegionRequest region;
    private ImageRequest searchImage;
    private AnchorsRequest anchors;
    private StateObjectDataRequest stateObjectData;
    private Long sceneId;
    private LocalDateTime timeStamp;
    private int timesActedOn;
}
