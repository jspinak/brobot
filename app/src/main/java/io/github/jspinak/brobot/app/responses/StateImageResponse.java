package io.github.jspinak.brobot.app.responses;

import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

@Getter
public class StateImageResponse {

    private Long projectId = 0L;
    private String name = "";
    private List<PatternResponse> patterns = new ArrayList<>();
    private String ownerStateName = "";
    private int timesActedOn = 0;
    private boolean shared = false;
    //private KmeansProfilesAllSchemas kmeansProfilesAllSchemas;
    //private ColorCluster colorCluster;
    private int index = 0;
    private boolean dynamic = false;
}
