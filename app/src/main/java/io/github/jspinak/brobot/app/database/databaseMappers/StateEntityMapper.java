package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.datatypes.state.state.State;
import org.springframework.stereotype.Component;

@Component
public class StateEntityMapper {

    private final StateImageEntityMapper stateImageEntityMapper;
    private final StateStringEntityMapper stateStringEntityMapper;
    private final StateRegionEntityMapper stateRegionEntityMapper;
    private final StateLocationEntityMapper stateLocationEntityMapper;
    private final ImageEntityMapper imageEntityMapper;
    private final MatchHistoryEntityMapper matchHistoryEntityMapper;
    private final RegionEmbeddableMapper regionEmbeddableMapper;

    public StateEntityMapper(StateImageEntityMapper stateImageEntityMapper,
                             StateStringEntityMapper stateStringEntityMapper,
                             StateRegionEntityMapper stateRegionEntityMapper,
                             StateLocationEntityMapper stateLocationEntityMapper,
                             ImageEntityMapper imageEntityMapper,
                             MatchHistoryEntityMapper matchHistoryEntityMapper,
                             RegionEmbeddableMapper regionEmbeddableMapper) {
        this.stateImageEntityMapper = stateImageEntityMapper;
        this.stateStringEntityMapper = stateStringEntityMapper;
        this.stateRegionEntityMapper = stateRegionEntityMapper;
        this.stateLocationEntityMapper = stateLocationEntityMapper;
        this.imageEntityMapper = imageEntityMapper;
        this.matchHistoryEntityMapper = matchHistoryEntityMapper;
        this.regionEmbeddableMapper = regionEmbeddableMapper;
    }
    
    public StateEntity map(State state) {
        StateEntity stateEntity = new StateEntity();
        stateEntity.setProjectId(state.getProjectId());
        stateEntity.setName(state.getName());
        stateEntity.setStateText(state.getStateText());
        stateEntity.setStateImages(stateImageEntityMapper.mapToStateImageEntitySet(state.getStateImages()));
        stateEntity.setStateStrings(stateStringEntityMapper.mapToStateStringEntitySet(state.getStateStrings()));
        stateEntity.setStateRegions(stateRegionEntityMapper.mapToStateRegionEntitySet(state.getStateRegions()));
        stateEntity.setStateLocations(stateLocationEntityMapper.mapToStateLocationEntitySet(state.getStateLocations()));
        stateEntity.setBlocking(state.isBlocking());
        stateEntity.setCanHide(state.getCanHide());
        stateEntity.setHidden(state.getHidden());
        stateEntity.setPathScore(state.getPathScore());
        stateEntity.setLastAccessed(state.getLastAccessed());
        stateEntity.setProbabilityExists(state.getProbabilityExists());
        stateEntity.setBaseProbabilityExists(state.getBaseProbabilityExists());
        stateEntity.setTimesVisited(state.getTimesVisited());
        stateEntity.setScenes(imageEntityMapper.mapToImageEntityList(state.getScenes()));
        stateEntity.setUsableArea(regionEmbeddableMapper.map(state.getUsableArea()));
        stateEntity.setMatchHistory(matchHistoryEntityMapper.map(state.getMatchHistory()));
        return stateEntity;
    }

    public State map(StateEntity stateEntity) {
        State state = new State();
        state.setProjectId(stateEntity.getProjectId());
        state.setName(stateEntity.getName());
        state.setStateText(stateEntity.getStateText());
        state.setStateImages(stateImageEntityMapper.mapToStateImageSet(stateEntity.getStateImages()));
        state.setStateStrings(stateStringEntityMapper.mapToStateStringSet(stateEntity.getStateStrings()));
        state.setStateRegions(stateRegionEntityMapper.mapToStateRegionSet(stateEntity.getStateRegions()));
        state.setStateLocations(stateLocationEntityMapper.mapToStateLocationSet(stateEntity.getStateLocations()));
        state.setBlocking(stateEntity.isBlocking());
        state.setCanHide(stateEntity.getCanHide());
        state.setHidden(stateEntity.getHidden());
        state.setPathScore(stateEntity.getPathScore());
        state.setLastAccessed(stateEntity.getLastAccessed());
        state.setProbabilityExists(stateEntity.getProbabilityExists());
        state.setBaseProbabilityExists(stateEntity.getBaseProbabilityExists());
        state.setTimesVisited(stateEntity.getTimesVisited());
        state.setScenes(imageEntityMapper.mapToImageList(stateEntity.getScenes()));
        state.setUsableArea(regionEmbeddableMapper.map(stateEntity.getUsableArea()));
        state.setMatchHistory(matchHistoryEntityMapper.map(stateEntity.getMatchHistory()));
        return state;
    }
}
