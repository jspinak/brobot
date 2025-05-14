package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.ProjectEntity;
import io.github.jspinak.brobot.datatypes.project.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectEntityMapper {

    private final Project project;

    public ProjectEntityMapper(Project project) {
        this.project = project;
    }

    public ProjectEntity mapWithoutStates() {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(project.getId());
        projectEntity.setName(project.getName());
        return projectEntity;
    }

}
