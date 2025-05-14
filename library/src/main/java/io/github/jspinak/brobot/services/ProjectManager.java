package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.datatypes.project.Project;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class ProjectManager {
    private final JsonParser jsonParser;

    private Project activeProject;

    public ProjectManager(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    public void loadProject(String json) throws ConfigurationException {
        // Direct deserialization
        this.activeProject = jsonParser.convertJson(json, Project.class);
    }
}
