package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class StateRequest {

    private String name = "";
    private Set<StateImageRequest> stateImages = new HashSet<>();
    private Set<String> stateText = new HashSet<>();
    private Set<StateStringRequest> stateStrings = new HashSet<>();
    private Set<StateRegionRequest> stateRegions = new HashSet<>();
    private Set<StateLocationRequest> stateLocations = new HashSet<>();
    private boolean blocking;
    private Set<String> canHide = new HashSet<>();
    private Set<String> hidden = new HashSet<>();
    private int pathScore;
    private LocalDateTime lastAccessed;
    private int baseProbabilityExists;
    private int probabilityExists;
    private int timesVisited;
    private List<SceneRequest> scenes = new ArrayList<>();
    private RegionRequest usableArea;
    private MatchHistoryRequest matchHistory = new MatchHistoryRequest();
}
