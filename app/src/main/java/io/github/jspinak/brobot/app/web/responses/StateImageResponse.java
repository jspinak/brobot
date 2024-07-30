package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL) // Include non-null properties only
@Getter
@Setter
public class StateImageResponse {

    private Long id;
    private Long projectId = 0L;
    private StateObject.Type objectType = StateObject.Type.IMAGE;
    private String name = "";
    private List<PatternResponse> patterns;
    private String ownerStateName = "";
    private int timesActedOn = 0;
    private boolean shared = false;
    private int index = 0;
    private boolean dynamic = false;
}