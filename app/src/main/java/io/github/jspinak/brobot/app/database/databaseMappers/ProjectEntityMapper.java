package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.app.database.entities.ProjectEntity;
import org.springframework.stereotype.Component;

@Component
public class ProjectEntityMapper {

    public ProjectEntity mapWithoutStates() {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(BrobotSettings.getCurrentProjectId());
        projectEntity.setName(BrobotSettings.getCurrentProjectName());
        return projectEntity;
    }

}
