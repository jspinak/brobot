package io.github.jspinak.brobot.datatypes.state.state;

import io.github.jspinak.brobot.datatypes.primitives.image.ImageResponse;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocationResponse;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegionResponse;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateStringResponse;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImageResponse;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class StateResponse {

    private Long id = 0L;
    private Long projectId = 0L;
    private String name = "";
    private Set<String> stateText = new HashSet<>();
    private Set<StateImageResponse> stateImages = new HashSet<>();
    private Set<StateStringResponse> stateStrings = new HashSet<>();
    private Set<StateRegionResponse> stateRegions = new HashSet<>();
    private Set<StateLocationResponse> stateLocations = new HashSet<>();
    private boolean blocking = false;
    private Set<String> canHide = new HashSet<>();
    private Set<String> hidden = new HashSet<>();
    private int pathScore = 1;
    private LocalDateTime lastAccessed = LocalDateTime.now();
    private int baseProbabilityExists = 100;
    private int probabilityExists = 0;
    private int timesVisited = 0;
    private final List<ImageResponse> scenes = new ArrayList<>();
    private final List<ImageResponse> illustrations = new ArrayList<>();

    public StateResponse(State state) {
        if (state == null) return;
        id = state.getId();
        projectId = state.getProjectId();
        name = state.getNameAsString();
        stateText = state.getStateText();
        state.getStateImages().forEach(image -> stateImages.add(new StateImageResponse(image)));
        state.getStateStrings().forEach(string -> stateStrings.add(new StateStringResponse(string)));
        state.getStateRegions().forEach(region -> stateRegions.add(new StateRegionResponse(region)));
        state.getStateLocations().forEach(location -> stateLocations.add(new StateLocationResponse(location)));
        blocking = state.isBlocking();
        canHide = state.getCanHide();
        hidden = state.getHidden();
        pathScore = state.getPathScore();
        lastAccessed = state.getLastAccessed();
        baseProbabilityExists = state.getBaseProbabilityExists();
        probabilityExists = state.getProbabilityExists();
        timesVisited = state.getTimesVisited();
        state.getScenes().forEach(scene -> scenes.add(scene.toImageResponse()));
        state.getIllustrations().forEach(ill -> illustrations.add(ill.toImageResponse()));
    }
}
