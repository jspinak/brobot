package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.ProjectEntity;
import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.database.repositories.ProjectRepository;
import io.github.jspinak.brobot.app.web.requests.ProjectRequest;
import io.github.jspinak.brobot.app.web.requests.StateRequest;
import io.github.jspinak.brobot.app.web.responses.ProjectResponse;
import io.github.jspinak.brobot.app.web.responses.StateResponse;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class StateResponseMapper {

    private final StateImageResponseMapper stateImageResponseMapper;
    private final StateStringResponseMapper stateStringResponseMapper;
    private final StateRegionResponseMapper stateRegionResponseMapper;
    private final StateLocationResponseMapper stateLocationResponseMapper;
    private final MatchHistoryResponseMapper matchHistoryResponseMapper;
    private final RegionResponseMapper regionResponseMapper;
    private final SceneResponseMapper sceneResponseMapper;
    private final ProjectRepository projectRepository;

    public StateResponseMapper(StateImageResponseMapper stateImageResponseMapper,
                               StateStringResponseMapper stateStringResponseMapper,
                               StateRegionResponseMapper stateRegionResponseMapper,
                               StateLocationResponseMapper stateLocationResponseMapper,
                               MatchHistoryResponseMapper matchHistoryResponseMapper,
                               RegionResponseMapper regionResponseMapper,
                               SceneResponseMapper sceneResponseMapper,
                               ProjectRepository projectRepository) {
        this.stateImageResponseMapper = stateImageResponseMapper;
        this.stateStringResponseMapper = stateStringResponseMapper;
        this.stateRegionResponseMapper = stateRegionResponseMapper;
        this.stateLocationResponseMapper = stateLocationResponseMapper;
        this.matchHistoryResponseMapper = matchHistoryResponseMapper;
        this.regionResponseMapper = regionResponseMapper;
        this.sceneResponseMapper = sceneResponseMapper;
        this.projectRepository = projectRepository;
    }

    public StateResponse map(StateEntity stateEntity) {
        StateResponse stateResponse = new StateResponse();
        stateResponse.setId(stateEntity.getId());
        // just set the ID and name to avoid a circular reference
        if (stateEntity.getProject() != null) {
            stateResponse.setProjectId(stateEntity.getProject().getId());
            stateResponse.setProjectName(stateEntity.getProject().getName());
        }
        stateResponse.setName(stateEntity.getName());
        stateEntity.getStateImages().forEach(image ->
                stateResponse.getStateImages().add(stateImageResponseMapper.map(image)));
        stateResponse.setStateText(new HashSet<>(stateEntity.getStateText()));
        stateEntity.getStateStrings().forEach(string ->
                stateResponse.getStateStrings().add(stateStringResponseMapper.map(string)));
        stateEntity.getStateRegions().forEach(region ->
                stateResponse.getStateRegions().add(stateRegionResponseMapper.map(region)));
        stateEntity.getStateLocations().forEach(location ->
                stateResponse.getStateLocations().add(stateLocationResponseMapper.map(location)));
        stateResponse.setBlocking(stateEntity.isBlocking());
        stateResponse.setCanHide(new HashSet<>(stateEntity.getCanHide()));
        stateResponse.setHidden(new HashSet<>(stateEntity.getHidden()));
        stateResponse.setPathScore(stateEntity.getPathScore());
        stateResponse.setLastAccessed(stateEntity.getLastAccessed());
        stateResponse.setBaseProbabilityExists(stateEntity.getBaseProbabilityExists());
        stateResponse.setProbabilityExists(stateEntity.getProbabilityExists());
        stateResponse.setTimesVisited(stateEntity.getTimesVisited());
        stateResponse.setScenes(sceneResponseMapper.mapToSceneResponseList(stateEntity.getScenes()));
        stateResponse.setUsableArea(regionResponseMapper.map(stateEntity.getUsableArea()));
        stateResponse.setMatchHistory(matchHistoryResponseMapper.map(stateEntity.getMatchHistory()));
        return stateResponse;
    }

    public StateEntity requestToEntity(StateRequest request) {
        if (request == null) return null;
        StateEntity entity = new StateEntity();
        entity.setName(request.getName());
        // Assuming you have a ProjectRepository to fetch the ProjectEntity
        if (request.getProjectRequest() != null) {
            ProjectEntity projectEntity = projectRepository.findById(request.getProjectRequest().getId())
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            entity.setProject(projectEntity);
        }
        if (request.getStateImages() != null) {
            entity.setStateImages(request.getStateImages().stream()
                    .map(stateImageResponseMapper::fromRequest)
                    .collect(Collectors.toSet()));
        }
        if (request.getStateText() != null) {
            entity.setStateText(new HashSet<>(request.getStateText()));
        }
        if (request.getStateStrings() != null) {
            entity.setStateStrings(request.getStateStrings().stream()
                    .map(stateStringResponseMapper::fromRequest)
                    .collect(Collectors.toSet()));
        }
        if (request.getStateRegions() != null) {
            entity.setStateRegions(request.getStateRegions().stream()
                    .map(stateRegionResponseMapper::fromRequest)
                    .collect(Collectors.toSet()));
        }
        if (request.getStateLocations() != null) {
            entity.setStateLocations(request.getStateLocations().stream()
                    .map(stateLocationResponseMapper::fromRequest)
                    .collect(Collectors.toSet()));
        }
        entity.setBlocking(request.isBlocking());
        if (request.getCanHide() != null) {
            entity.setCanHide(new HashSet<>(request.getCanHide()));
        }
        if (request.getHidden() != null) {
            entity.setHidden(new HashSet<>(request.getHidden()));
        }
        entity.setPathScore(request.getPathScore());
        entity.setLastAccessed(request.getLastAccessed());
        entity.setBaseProbabilityExists(request.getBaseProbabilityExists());
        entity.setProbabilityExists(request.getProbabilityExists());
        entity.setTimesVisited(request.getTimesVisited());
        if (request.getScenes() != null) {
            entity.setScenes(request.getScenes().stream()
                    .map(sceneResponseMapper::fromRequest)
                    .collect(Collectors.toList()));
        }
        if (request.getUsableArea() != null) {
            entity.setUsableArea(regionResponseMapper.fromRequest(request.getUsableArea()));
        }
        if (request.getMatchHistory() != null) {
            entity.setMatchHistory(matchHistoryResponseMapper.fromRequest(request.getMatchHistory()));
        }
        return entity;
    }

    public StateResponse mapWithoutProject(StateEntity stateEntity) {
        StateResponse stateResponse = new StateResponse();
        stateResponse.setId(stateEntity.getId());
        stateResponse.setName(stateEntity.getName());
        stateEntity.getStateImages().forEach(image ->
                stateResponse.getStateImages().add(stateImageResponseMapper.map(image)));
        stateResponse.setStateText(new HashSet<>(stateEntity.getStateText()));
        stateEntity.getStateStrings().forEach(string ->
                stateResponse.getStateStrings().add(stateStringResponseMapper.map(string)));
        stateEntity.getStateRegions().forEach(region ->
                stateResponse.getStateRegions().add(stateRegionResponseMapper.map(region)));
        stateEntity.getStateLocations().forEach(location ->
                stateResponse.getStateLocations().add(stateLocationResponseMapper.map(location)));
        stateResponse.setBlocking(stateEntity.isBlocking());
        stateResponse.setCanHide(new HashSet<>(stateEntity.getCanHide()));
        stateResponse.setHidden(new HashSet<>(stateEntity.getHidden()));
        stateResponse.setPathScore(stateEntity.getPathScore());
        stateResponse.setLastAccessed(stateEntity.getLastAccessed());
        stateResponse.setBaseProbabilityExists(stateEntity.getBaseProbabilityExists());
        stateResponse.setProbabilityExists(stateEntity.getProbabilityExists());
        stateResponse.setTimesVisited(stateEntity.getTimesVisited());
        stateResponse.setScenes(sceneResponseMapper.mapToSceneResponseList(stateEntity.getScenes()));
        stateResponse.setUsableArea(regionResponseMapper.map(stateEntity.getUsableArea()));
        stateResponse.setMatchHistory(matchHistoryResponseMapper.map(stateEntity.getMatchHistory()));
        return stateResponse;
    }

}
