package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.StateLocationEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class StateLocationEntityMapper {

    private final LocationEntityMapper locationEntityMapper;
    private final PositionEmbeddableMapper positionEmbeddableMapper;
    private final AnchorsEntityMapper anchorsEntityMapper;
    private final MatchHistoryEntityMapper matchHistoryEntityMapper;

    public StateLocationEntityMapper(LocationEntityMapper locationEntityMapper,
                                     PositionEmbeddableMapper positionEmbeddableMapper,
                                     AnchorsEntityMapper anchorsEntityMapper,
                                     MatchHistoryEntityMapper matchHistoryEntityMapper) {
        this.locationEntityMapper = locationEntityMapper;
        this.positionEmbeddableMapper = positionEmbeddableMapper;
        this.anchorsEntityMapper = anchorsEntityMapper;
        this.matchHistoryEntityMapper = matchHistoryEntityMapper;
    }
    
    public StateLocationEntity map(StateLocation stateLocation) {
        StateLocationEntity stateLocationEntity = new StateLocationEntity();
        stateLocationEntity.setObjectType(stateLocation.getObjectType());
        stateLocationEntity.setName(stateLocation.getName());
        stateLocationEntity.setLocation(locationEntityMapper.map(stateLocation.getLocation()));
        stateLocationEntity.setOwnerStateName(stateLocation.getOwnerStateName());
        stateLocationEntity.setStaysVisibleAfterClicked(stateLocation.getStaysVisibleAfterClicked());
        stateLocationEntity.setProbabilityExists(stateLocationEntity.getProbabilityExists());
        stateLocationEntity.setTimesActedOn(stateLocation.getTimesActedOn());
        stateLocationEntity.setPosition(positionEmbeddableMapper.map(stateLocation.getPosition()));
        stateLocationEntity.setAnchors(anchorsEntityMapper.map(stateLocation.getAnchors()));
        stateLocationEntity.setMatchHistory(matchHistoryEntityMapper.map(stateLocation.getMatchHistory()));
        return stateLocationEntity;
    }

    public StateLocation map(StateLocationEntity stateLocationEntity) {
        StateLocation stateLocation = new StateLocation();
        stateLocation.setObjectType(stateLocationEntity.getObjectType());
        stateLocation.setName(stateLocationEntity.getName());
        stateLocation.setLocation(locationEntityMapper.map(stateLocationEntity.getLocation()));
        stateLocation.setOwnerStateName(stateLocationEntity.getOwnerStateName());
        stateLocation.setStaysVisibleAfterClicked(stateLocationEntity.getStaysVisibleAfterClicked());
        stateLocation.setProbabilityExists(stateLocationEntity.getProbabilityExists());
        stateLocation.setTimesActedOn(stateLocationEntity.getTimesActedOn());
        stateLocation.setPosition(positionEmbeddableMapper.map(stateLocationEntity.getPosition()));
        stateLocation.setAnchors(anchorsEntityMapper.map(stateLocationEntity.getAnchors()));
        stateLocation.setMatchHistory(matchHistoryEntityMapper.map(stateLocationEntity.getMatchHistory()));
        return stateLocation;
    }

    public Set<StateLocationEntity> mapToStateLocationEntitySet(Set<StateLocation> stateLocations) {
        return new HashSet<>(mapToStateLocationEntityList(stateLocations.stream().toList()));
    }

    public List<StateLocationEntity> mapToStateLocationEntityList(List<StateLocation> stateLocations) {
        List<StateLocationEntity> stateLocationEntityList = new ArrayList<>();
        stateLocations.forEach(stateLocation -> stateLocationEntityList.add(map(stateLocation)));
        return stateLocationEntityList;
    }

    public Set<StateLocation> mapToStateLocationSet(Set<StateLocationEntity> stateLocationEntities) {
        return new HashSet<>(mapToStateLocationList(stateLocationEntities.stream().toList()));
    }

    public List<StateLocation> mapToStateLocationList(List<StateLocationEntity> stateLocationEntities) {
        List<StateLocation> stateLocationList = new ArrayList<>();
        stateLocationEntities.forEach(stateLocationEntity -> stateLocationList.add(map(stateLocationEntity)));
        return stateLocationList;
    }
}
