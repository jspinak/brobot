package com.brobot.app.responses;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class StateResponse {

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

}
