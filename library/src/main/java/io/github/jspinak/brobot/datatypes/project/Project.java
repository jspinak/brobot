package io.github.jspinak.brobot.datatypes.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {
    private Long id = 0L;
    private String name;
    private String description;
    private String version;
    private LocalDateTime created;
    private LocalDateTime updated;
    private List<State> states;
    private List<StateTransitions> stateTransitions;
    private AutomationUI automation;
    private ProjectConfiguration configuration;

    public void reset() {
        id = null;
        name = null;
        description = null;
        version = null;
        created = null;
        updated = null;
        states = null;
        stateTransitions = null;
        automation = null;
        configuration = null;
    }

}