package io.github.jspinak.brobot.datatypes.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {
    private static Project instance;
    private Long id = 0L;
    private String name;
    private String description;
    private String version;
    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") // If needed
    private LocalDateTime created; // Or String, or java.util.Date
    private LocalDateTime updated;
    private List<State> states;
    private List<StateTransitions> stateTransitions;
    private AutomationUI automation; // Assuming AutomationUI is a POJO for the "automation" object
    private ProjectConfiguration configuration; // Assuming ProjectConfiguration is a POJO for "configuration"


    private Project() {}

    public static synchronized Project getInstance() {
        if (instance == null) {
            instance = new Project();
        }
        return instance;
    }

    public void setProject(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void reset() {
        id = null;
        name = null;
    }
}
