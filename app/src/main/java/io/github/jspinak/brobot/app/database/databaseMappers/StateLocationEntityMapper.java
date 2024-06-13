package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.StateLocationEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StateLocationEntityMapper {
    
    public static StateLocationEntity map(StateLocation stateLocation) {
        StateLocationEntity stateLocationEntity = new StateLocationEntity();
        stateLocationEntity.setObjectType(stateLocation.getObjectType());
        stateLocationEntity.setName(stateLocation.getName());
        stateLocationEntity.setLocation(LocationEntityMapper.map(stateLocation.getLocation()));
        stateLocationEntity.setOwnerStateName(stateLocation.getOwnerStateName());
        stateLocationEntity.setStaysVisibleAfterClicked(stateLocation.getStaysVisibleAfterClicked());
        stateLocationEntity.setProbabilityExists(stateLocationEntity.getProbabilityExists());
        stateLocationEntity.setTimesActedOn(stateLocation.getTimesActedOn());
        stateLocationEntity.setPosition(PositionEmbeddableMapper.map(stateLocation.getPosition()));
        stateLocationEntity.setAnchors(AnchorsEntityMapper.map(stateLocation.getAnchors()));
        stateLocationEntity.setMatchHistory(MatchHistoryEntityMapper.map(stateLocation.getMatchHistory()));
        return stateLocationEntity;
    }

    public static StateLocation map(StateLocationEntity stateLocationEntity) {
        StateLocation stateLocation = new StateLocation();
        stateLocation.setObjectType(stateLocationEntity.getObjectType());
        stateLocation.setName(stateLocationEntity.getName());
        stateLocation.setLocation(LocationEntityMapper.map(stateLocationEntity.getLocation()));
        stateLocation.setOwnerStateName(stateLocationEntity.getOwnerStateName());
        stateLocation.setStaysVisibleAfterClicked(stateLocationEntity.getStaysVisibleAfterClicked());
        stateLocation.setProbabilityExists(stateLocationEntity.getProbabilityExists());
        stateLocation.setTimesActedOn(stateLocationEntity.getTimesActedOn());
        stateLocation.setPosition(PositionEmbeddableMapper.map(stateLocationEntity.getPosition()));
        stateLocation.setAnchors(AnchorsEntityMapper.map(stateLocationEntity.getAnchors()));
        stateLocation.setMatchHistory(MatchHistoryEntityMapper.map(stateLocationEntity.getMatchHistory()));
        return stateLocation;
    }

    public static Set<StateLocationEntity> mapToStateLocationEntitySet(Set<StateLocation> stateLocations) {
        return new HashSet<>(mapToStateLocationEntityList(stateLocations.stream().toList()));
    }

    public static List<StateLocationEntity> mapToStateLocationEntityList(List<StateLocation> stateLocations) {
        List<StateLocationEntity> stateLocationEntityList = new ArrayList<>();
        stateLocations.forEach(stateLocation -> stateLocationEntityList.add(map(stateLocation)));
        return stateLocationEntityList;
    }

    public static Set<StateLocation> mapToStateLocationSet(Set<StateLocationEntity> stateLocationEntities) {
        return new HashSet<>(mapToStateLocationList(stateLocationEntities.stream().toList()));
    }

    public static List<StateLocation> mapToStateLocationList(List<StateLocationEntity> stateLocationEntities) {
        List<StateLocation> stateLocationList = new ArrayList<>();
        stateLocationEntities.forEach(stateLocationEntity -> stateLocationList.add(map(stateLocationEntity)));
        return stateLocationList;
    }
}
