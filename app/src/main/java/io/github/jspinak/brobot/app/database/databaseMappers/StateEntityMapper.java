package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.services.SceneService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import org.springframework.stereotype.Component;

@Component
public class StateEntityMapper {

    private final StateImageEntityMapper stateImageEntityMapper;
    private final StateStringEntityMapper stateStringEntityMapper;
    private final StateRegionEntityMapper stateRegionEntityMapper;
    private final StateLocationEntityMapper stateLocationEntityMapper;
    private final SceneEntityMapper sceneEntityMapper;
    private final MatchHistoryEntityMapper matchHistoryEntityMapper;
    private final RegionEmbeddableMapper regionEmbeddableMapper;
    private final SceneService sceneService;

    public StateEntityMapper(StateImageEntityMapper stateImageEntityMapper,
                             StateStringEntityMapper stateStringEntityMapper,
                             StateRegionEntityMapper stateRegionEntityMapper,
                             StateLocationEntityMapper stateLocationEntityMapper,
                             SceneEntityMapper sceneEntityMapper,
                             MatchHistoryEntityMapper matchHistoryEntityMapper,
                             RegionEmbeddableMapper regionEmbeddableMapper,
                             SceneService sceneService) {
        this.stateImageEntityMapper = stateImageEntityMapper;
        this.stateStringEntityMapper = stateStringEntityMapper;
        this.stateRegionEntityMapper = stateRegionEntityMapper;
        this.stateLocationEntityMapper = stateLocationEntityMapper;
        this.sceneEntityMapper = sceneEntityMapper;
        this.matchHistoryEntityMapper = matchHistoryEntityMapper;
        this.regionEmbeddableMapper = regionEmbeddableMapper;
        this.sceneService = sceneService;
    }
    
    public StateEntity map(State state) {
        StateEntity stateEntity = new StateEntity(state.getId());
        stateEntity.setProjectId(state.getProjectId());
        stateEntity.setName(state.getName());
        stateEntity.setStateText(state.getStateText());
        stateEntity.setStateImages(stateImageEntityMapper.mapToStateImageEntitySet(state.getStateImages()));
        stateEntity.setStateStrings(stateStringEntityMapper.mapToStateStringEntitySet(state.getStateStrings()));
        stateEntity.setStateRegions(stateRegionEntityMapper.mapToStateRegionEntitySet(state.getStateRegions()));
        stateEntity.setStateLocations(stateLocationEntityMapper.mapToStateLocationEntitySet(state.getStateLocations()));
        stateEntity.setBlocking(state.isBlocking());
        stateEntity.setCanHide(state.getCanHide());
        stateEntity.setCanHideIds(state.getCanHideIds());
        stateEntity.setHidden(state.getHiddenStateNames());
        stateEntity.setHiddenStateIds(state.getHiddenStateIds());
        stateEntity.setPathScore(state.getPathScore());
        stateEntity.setLastAccessed(state.getLastAccessed());
        stateEntity.setProbabilityExists(state.getProbabilityExists());
        stateEntity.setBaseProbabilityExists(state.getBaseProbabilityExists());
        stateEntity.setTimesVisited(state.getTimesVisited());
        stateEntity.setScenes(sceneService.mapToSceneEntityList(state.getScenes()));
        stateEntity.setUsableArea(regionEmbeddableMapper.map(state.getUsableArea()));
        stateEntity.setMatchHistory(matchHistoryEntityMapper.map(state.getMatchHistory()));
        return stateEntity;
    }

    public State map(StateEntity stateEntity) {
        State state = new State();
        state.setProjectId(stateEntity.getProjectId());
        state.setId(stateEntity.getId());
        state.setName(stateEntity.getName());
        state.setStateText(stateEntity.getStateText());
        state.setStateImages(stateImageEntityMapper.mapToStateImageSet(stateEntity.getStateImages()));
        state.setStateStrings(stateStringEntityMapper.mapToStateStringSet(stateEntity.getStateStrings()));
        state.setStateRegions(stateRegionEntityMapper.mapToStateRegionSet(stateEntity.getStateRegions()));
        state.setStateLocations(stateLocationEntityMapper.mapToStateLocationSet(stateEntity.getStateLocations()));
        state.setBlocking(stateEntity.isBlocking());
        state.setCanHide(stateEntity.getCanHide());
        state.setCanHideIds(stateEntity.getCanHideIds());
        state.setHiddenStateNames(stateEntity.getHidden());
        state.setHiddenStateIds(stateEntity.getHiddenStateIds());
        state.setPathScore(stateEntity.getPathScore());
        state.setLastAccessed(stateEntity.getLastAccessed());
        state.setProbabilityExists(stateEntity.getProbabilityExists());
        state.setBaseProbabilityExists(stateEntity.getBaseProbabilityExists());
        state.setTimesVisited(stateEntity.getTimesVisited());
        state.setScenes(sceneEntityMapper.mapToSceneList(stateEntity.getScenes()));
        state.setUsableArea(regionEmbeddableMapper.map(stateEntity.getUsableArea()));
        state.setMatchHistory(matchHistoryEntityMapper.map(stateEntity.getMatchHistory()));
        return state;
    }
}
