package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.web.requests.StateRequest;
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

    public StateResponseMapper(StateImageResponseMapper stateImageResponseMapper,
                               StateStringResponseMapper stateStringResponseMapper,
                               StateRegionResponseMapper stateRegionResponseMapper,
                               StateLocationResponseMapper stateLocationResponseMapper,
                               MatchHistoryResponseMapper matchHistoryResponseMapper,
                               RegionResponseMapper regionResponseMapper, SceneResponseMapper sceneResponseMapper) {
        this.stateImageResponseMapper = stateImageResponseMapper;
        this.stateStringResponseMapper = stateStringResponseMapper;
        this.stateRegionResponseMapper = stateRegionResponseMapper;
        this.stateLocationResponseMapper = stateLocationResponseMapper;
        this.matchHistoryResponseMapper = matchHistoryResponseMapper;
        this.regionResponseMapper = regionResponseMapper;
        this.sceneResponseMapper = sceneResponseMapper;
    }

    public StateResponse map(StateEntity stateEntity) {
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

    public StateEntity map(StateResponse stateResponse) {
        StateEntity stateEntity = new StateEntity();
        stateEntity.setId(stateResponse.getId());
        stateEntity.setName(stateResponse.getName());
        stateResponse.getStateImages().forEach(image ->
                stateEntity.getStateImages().add(stateImageResponseMapper.map(image)));
        stateEntity.setStateText(new HashSet<>(stateResponse.getStateText()));
        stateResponse.getStateStrings().forEach(string ->
                stateEntity.getStateStrings().add(stateStringResponseMapper.map(string)));
        stateResponse.getStateRegions().forEach(region ->
                stateEntity.getStateRegions().add(stateRegionResponseMapper.map(region)));
        stateResponse.getStateLocations().forEach(location ->
                stateEntity.getStateLocations().add(stateLocationResponseMapper.map(location)));
        stateEntity.setBlocking(stateResponse.isBlocking());
        stateEntity.setCanHide(new HashSet<>(stateResponse.getCanHide()));
        stateEntity.setHidden(new HashSet<>(stateResponse.getHidden()));
        stateEntity.setPathScore(stateResponse.getPathScore());
        stateEntity.setLastAccessed(stateResponse.getLastAccessed());
        stateEntity.setBaseProbabilityExists(stateResponse.getBaseProbabilityExists());
        stateEntity.setProbabilityExists(stateResponse.getProbabilityExists());
        stateEntity.setTimesVisited(stateResponse.getTimesVisited());
        stateEntity.setScenes(sceneResponseMapper.mapToSceneEntityList(stateResponse.getScenes()));
        stateEntity.setUsableArea(regionResponseMapper.map(stateResponse.getUsableArea()));
        stateEntity.setMatchHistory(matchHistoryResponseMapper.map(stateResponse.getMatchHistory()));
        return stateEntity;
    }

    public StateEntity fromRequest(StateRequest request) {
        if (request == null) return null;
        StateEntity entity = new StateEntity();
        entity.setName(request.getName());
        entity.setStateImages(request.getStateImages().stream()
                .map(stateImageResponseMapper::fromRequest)
                .collect(Collectors.toSet()));
        entity.setStateText(new HashSet<>(request.getStateText()));
        entity.setStateStrings(request.getStateStrings().stream()
                .map(stateStringResponseMapper::fromRequest)
                .collect(Collectors.toSet()));
        entity.setStateRegions(request.getStateRegions().stream()
                .map(stateRegionResponseMapper::fromRequest)
                .collect(Collectors.toSet()));
        entity.setStateLocations(request.getStateLocations().stream()
                .map(stateLocationResponseMapper::fromRequest)
                .collect(Collectors.toSet()));
        entity.setBlocking(request.isBlocking());
        entity.setCanHide(new HashSet<>(request.getCanHide()));
        entity.setHidden(new HashSet<>(request.getHidden()));
        entity.setPathScore(request.getPathScore());
        entity.setLastAccessed(request.getLastAccessed());
        entity.setBaseProbabilityExists(request.getBaseProbabilityExists());
        entity.setProbabilityExists(request.getProbabilityExists());
        entity.setTimesVisited(request.getTimesVisited());
        entity.setScenes(request.getScenes().stream()
                .map(sceneResponseMapper::fromRequest)
                .collect(Collectors.toList()));
        entity.setUsableArea(regionResponseMapper.fromRequest(request.getUsableArea()));
        entity.setMatchHistory(matchHistoryResponseMapper.fromRequest(request.getMatchHistory()));
        return entity;
    }
}
