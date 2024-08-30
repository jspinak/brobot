package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ObjectCollectionRequest {
    private Long id;
    private List<StateLocationRequest> stateLocations;
    private List<StateImageRequest> stateImages;
    private List<StateRegionRequest> stateRegions;
    private List<StateStringRequest> stateStrings;
    private List<MatchesRequest> matches;
    private List<SceneRequest> scenes;
}
