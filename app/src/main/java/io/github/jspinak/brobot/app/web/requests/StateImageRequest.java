package io.github.jspinak.brobot.app.web.requests;

import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class StateImageRequest {
    private Long id;
    private Long projectId = 0L;
    private StateObject.Type objectType = StateObject.Type.IMAGE;
    private String name = "";
    private List<PatternRequest> patterns = new ArrayList<>();
    private Long ownerStateId;
    private String ownerStateName = "";
    private int timesActedOn = 0;
    private boolean shared = false;
    private int index = 0;
    private boolean dynamic = false;
    private Set<Long> involvedTransitionIds = new HashSet<>();
}
