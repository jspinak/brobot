package io.github.jspinak.brobot.datatypes.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {
    private Long id = 0L;
    private String name;
    private String description;
    private String version;
    private String author;
    private LocalDateTime created;
    private LocalDateTime updated;
    private List<State> states;
    private List<StateTransitions> stateTransitions;
    private AutomationUI automation;
    private ProjectConfiguration configuration;
    private String organization;
    private String website;
    private String license;
    private String createdDate;
    private Map<String, Object> customProperties = new HashMap<>();

    public void reset() {
        id = null;
        name = null;
        description = null;
        version = null;
        author = null;
        created = null;
        updated = null;
        states = null;
        stateTransitions = null;
        automation = null;
        configuration = null;
        organization = null;
        website = null;
        license = null;
        createdDate = null;
        customProperties = new HashMap<>();
    }

}