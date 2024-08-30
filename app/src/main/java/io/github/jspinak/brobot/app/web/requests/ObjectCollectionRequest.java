package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ObjectCollectionRequest {
    private Long id;
    private List<StateLocationRequest> stateLocations = new ArrayList<>();
    private List<StateImageRequest> stateImages = new ArrayList<>();
    private List<StateRegionRequest> stateRegions = new ArrayList<>();
    private List<StateStringRequest> stateStrings = new ArrayList<>();
    private List<MatchesRequest> matches = new ArrayList<>();
    private List<SceneRequest> scenes = new ArrayList<>();
}
