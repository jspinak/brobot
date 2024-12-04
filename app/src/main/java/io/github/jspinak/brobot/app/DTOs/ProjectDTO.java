package io.github.jspinak.brobot.app.DTOs;

import io.github.jspinak.brobot.app.database.entities.ProjectEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectDTO {
    private Long id; // internal database id
    private Long brobotProjectId;
    private String name;

    public ProjectDTO(Long brobotProjectId, String name) {
        this.brobotProjectId = brobotProjectId;
        this.name = name;
    }

    public static ProjectDTO fromEntity(ProjectEntity projectEntity) {
        return new ProjectDTO(projectEntity.getId(), projectEntity.getName());
    }
}
