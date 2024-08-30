package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL) // Include non-null properties only
@Getter
@Setter
public class StateResponse {

    private Long id;
    private String name = "";
    private Set<StateImageResponse> stateImages = new HashSet<>();
    private Set<String> stateText = new HashSet<>();
    private Set<StateStringResponse> stateStrings = new HashSet<>();
    private Set<StateRegionResponse> stateRegions = new HashSet<>();
    private Set<StateLocationResponse> stateLocations = new HashSet<>();
    private boolean blocking;
    private Set<String> canHide = new HashSet<>();
    private Set<String> hidden = new HashSet<>();
    private int pathScore;
    private LocalDateTime lastAccessed;
    private int baseProbabilityExists;
    private int probabilityExists;
    private int timesVisited;
    private List<SceneResponse> scenes = new ArrayList<>();
    private RegionResponse usableArea;
    private MatchHistoryResponse matchHistory = new MatchHistoryResponse();
}
