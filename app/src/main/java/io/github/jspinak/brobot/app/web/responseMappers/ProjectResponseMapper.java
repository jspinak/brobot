package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.ProjectEntity;
import io.github.jspinak.brobot.app.web.requests.ProjectRequest;
import io.github.jspinak.brobot.app.web.responses.ProjectResponse;
import org.springframework.stereotype.Component;

@Component
public class ProjectResponseMapper {

    private final StateResponseMapper stateResponseMapper;

    public ProjectResponseMapper(StateResponseMapper stateResponseMapper) {
        this.stateResponseMapper = stateResponseMapper;
    }

    public ProjectResponse entityToResponse(ProjectEntity projectEntity) {
        ProjectResponse response = new ProjectResponse();
        response.setId(projectEntity.getId());
        response.setName(projectEntity.getName());
        projectEntity.getStates().forEach(stateEntity ->
                response.getStates().add(stateResponseMapper.mapWithoutProject(stateEntity)));
        return response;
    }

    /**
     * ProjectRequest doesn't have states or an id since it is created only with a name.
     * @param projectRequest the new project
     * @return a ProjectEntity
     */
    public ProjectEntity requestToEntity(ProjectRequest projectRequest) {
        if (projectRequest == null) {
            return null;
        }
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName(projectEntity.getName());
        return projectEntity;
    }
}
