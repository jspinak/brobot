package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.datatypes.primitives.image.ImageResponse;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.LocationResponse;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.region.RegionResponse;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MatchResponse {

    private Long id = 0L;
    private double score = 0.0;
    private LocationResponse target = new LocationResponse(new Location());
    private ImageResponse image = new ImageResponse(null);
    private String text = "";
    private String name = "";
    private RegionResponse region = new RegionResponse(new Region());
    private ImageResponse searchImage = new ImageResponse(null);
    private Anchors anchors = new Anchors();
    private StateObjectData stateObjectData = new StateObjectData();
    //private Mat histogram;
    private ImageResponse scene = new ImageResponse(null);
    private LocalDateTime timeStamp = LocalDateTime.now();
    private int timesActedOn = 0;

    public MatchResponse(Match match) {
        if (match == null) return;
        id = match.getId();
        score = match.getScore();
        target = new LocationResponse(match.getLocation());
        image = new ImageResponse(match.getImage());
        text = match.getText();
        name = match.getName();
        region = new RegionResponse(match.getRegion());
        searchImage = new ImageResponse(match.getSearchImage());
        anchors = match.getAnchors();
        stateObjectData = match.getStateObjectData();
        scene = match.getScene().toImageResponse();
        timeStamp = match.getTimeStamp();
        timesActedOn = match.getTimesActedOn();
    }

}
