package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.datatypes.state.state.State;

public class StateEntityMapper {
    
    public static StateEntity map(State state) {
        StateEntity stateEntity = new StateEntity();
        stateEntity.setProjectId(state.getProjectId());
        stateEntity.setName(state.getName());
        stateEntity.setStateText(state.getStateText());
        stateEntity.setStateImages(StateImageEntityMapper.mapToStateImageEntitySet(state.getStateImages()));
        stateEntity.setStateStrings(StateStringEntityMapper.mapToStateStringEntitySet(state.getStateStrings()));
        stateEntity.setStateRegions(StateRegionEntityMapper.mapToStateRegionEntitySet(state.getStateRegions()));
        stateEntity.setStateLocations(StateLocationEntityMapper.mapToStateLocationEntitySet(state.getStateLocations()));
        stateEntity.setBlocking(state.isBlocking());
        stateEntity.setCanHide(state.getCanHide());
        stateEntity.setHidden(state.getHidden());
        stateEntity.setPathScore(state.getPathScore());
        stateEntity.setLastAccessed(state.getLastAccessed());
        stateEntity.setProbabilityExists(state.getProbabilityExists());
        stateEntity.setBaseProbabilityExists(state.getBaseProbabilityExists());
        stateEntity.setTimesVisited(state.getTimesVisited());
        stateEntity.setScenes(ImageEntityMapper.mapToImageEntityList(state.getScenes()));
        stateEntity.setIllustrations(state.getIllustrations());
        stateEntity.setMatchHistory(MatchHistoryEntityMapper.map(state.getMatchHistory()));
        return stateEntity;
    }

    public static State map(StateEntity stateEntity) {
        State state = new State();
        state.setProjectId(stateEntity.getProjectId());
        state.setName(stateEntity.getName());
        state.setStateText(stateEntity.getStateText());
        state.setStateImages(StateImageEntityMapper.mapToStateImageSet(stateEntity.getStateImages()));
        state.setStateStrings(StateStringEntityMapper.mapToStateStringSet(stateEntity.getStateStrings()));
        state.setStateRegions(StateRegionEntityMapper.mapToStateRegionSet(stateEntity.getStateRegions()));
        state.setStateLocations(StateLocationEntityMapper.mapToStateLocationSet(stateEntity.getStateLocations()));
        state.setBlocking(stateEntity.isBlocking());
        state.setCanHide(stateEntity.getCanHide());
        state.setHidden(stateEntity.getHidden());
        state.setPathScore(stateEntity.getPathScore());
        state.setLastAccessed(stateEntity.getLastAccessed());
        state.setProbabilityExists(stateEntity.getProbabilityExists());
        state.setBaseProbabilityExists(stateEntity.getBaseProbabilityExists());
        state.setTimesVisited(stateEntity.getTimesVisited());
        state.setScenes(ImageEntityMapper.mapToImageList(stateEntity.getScenes()));
        state.setIllustrations(stateEntity.getIllustrations());
        state.setMatchHistory(MatchHistoryEntityMapper.map(stateEntity.getMatchHistory()));
        return state;
    }
}
