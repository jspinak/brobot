package io.github.jspinak.brobot.datatypes.state.stateObject.stateImage;

import io.github.jspinak.brobot.datatypes.primitives.image.PatternResponse;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class StateImageResponse {

    private Long id = 0L;
    private String name = "";
    private List<PatternResponse> patterns = new ArrayList<>();
    private String ownerStateName = "";
    private int timesActedOn = 0;
    private boolean shared = false;
    //private KmeansProfilesAllSchemas kmeansProfilesAllSchemas;
    //private ColorCluster colorCluster;
    private int index = 0;
    private boolean dynamic = false;

    public StateImageResponse(StateImage stateImage) {
        if (stateImage == null) return;
        id = stateImage.getId();
        name = stateImage.getName() != null ? stateImage.getName() : "";
        stateImage.getPatterns().forEach(pattern -> patterns.add(new PatternResponse(pattern)));
        ownerStateName = stateImage.getOwnerStateName();
        timesActedOn = stateImage.getTimesActedOn();
        shared = stateImage.isShared();
        index = stateImage.getIndex();
        dynamic = stateImage.isDynamic();
    }

}
