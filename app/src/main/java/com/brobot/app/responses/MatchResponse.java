package com.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MatchResponse {

    private double score = 0.0;
    private LocationResponse target = new LocationResponse();
    private ImageResponse image = new ImageResponse();
    private String text = "";
    private String name = "";
    private RegionResponse region = new RegionResponse();
    private ImageResponse searchImage = new ImageResponse();
    private AnchorsResponse anchors = new AnchorsResponse();
    private StateObjectData stateObjectData = new StateObjectData();
    //private Mat histogram;
    private ImageResponse scene = new ImageResponse();
    private LocalDateTime timeStamp = LocalDateTime.now();
    private int timesActedOn = 0;

}
