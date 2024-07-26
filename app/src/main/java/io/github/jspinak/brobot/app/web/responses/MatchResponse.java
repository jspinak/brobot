package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class MatchResponse {
    private Long id;
    private double score;
    private LocationResponse target;
    private ImageResponse image;
    private String text;
    private String name;
    private RegionResponse region;
    private ImageResponse searchImage;
    private AnchorsResponse anchors;
    private StateObjectDataResponse stateObjectData;
    private ImageResponse scene;
    private LocalDateTime timeStamp;
    private int timesActedOn;
}

