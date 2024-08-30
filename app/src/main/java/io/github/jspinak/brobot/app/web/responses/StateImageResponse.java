package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL) // Include non-null properties only
@Getter
@Setter
public class StateImageResponse {

    private Long id;
    private Long projectId = 0L;
    private StateObject.Type objectType = StateObject.Type.IMAGE;
    private String name = "";
    private List<PatternResponse> patterns = new ArrayList<>();
    private Long ownerStateId;
    private String ownerStateName = "";
    private int timesActedOn = 0;
    private boolean shared = false;
    private int index = 0;
    private boolean dynamic = false;
    private Set<Long> involvedTransitionIds = new HashSet<>();
}