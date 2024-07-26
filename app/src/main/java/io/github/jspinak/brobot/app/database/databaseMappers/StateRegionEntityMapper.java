package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.StateRegionEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class StateRegionEntityMapper {

    private final RegionEmbeddableMapper regionEmbeddableMapper;
    private final PositionEmbeddableMapper positionEmbeddableMapper;
    private final AnchorsEntityMapper anchorsEntityMapper;
    private final MatchHistoryEntityMapper matchHistoryEntityMapper;

    public StateRegionEntityMapper(RegionEmbeddableMapper regionEmbeddableMapper,
                                   PositionEmbeddableMapper positionEmbeddableMapper,
                                   AnchorsEntityMapper anchorsEntityMapper,
                                   MatchHistoryEntityMapper matchHistoryEntityMapper) {
        this.regionEmbeddableMapper = regionEmbeddableMapper;
        this.positionEmbeddableMapper = positionEmbeddableMapper;
        this.anchorsEntityMapper = anchorsEntityMapper;
        this.matchHistoryEntityMapper = matchHistoryEntityMapper;
    }
    
    public StateRegionEntity map(StateRegion stateRegion) {
        StateRegionEntity stateRegionEntity = new StateRegionEntity();
        stateRegionEntity.setObjectType(stateRegion.getObjectType());
        stateRegionEntity.setName(stateRegion.getName());
        stateRegionEntity.setSearchRegion(regionEmbeddableMapper.map(stateRegion.getSearchRegion()));
        stateRegionEntity.setOwnerStateName(stateRegion.getOwnerStateName());
        stateRegionEntity.setStaysVisibleAfterClicked(stateRegion.getStaysVisibleAfterClicked());
        stateRegionEntity.setProbabilityExists(stateRegion.getProbabilityExists());
        stateRegionEntity.setTimesActedOn(stateRegion.getTimesActedOn());
        stateRegionEntity.setPosition(positionEmbeddableMapper.map(stateRegion.getPosition()));
        stateRegionEntity.setAnchors(anchorsEntityMapper.map(stateRegion.getAnchors()));
        stateRegionEntity.setMockText(stateRegion.getMockText());
        stateRegionEntity.setMatchHistory(matchHistoryEntityMapper.map(stateRegion.getMatchHistory()));
        return stateRegionEntity;
    }
    public StateRegion map(StateRegionEntity stateRegionEntity) {
        StateRegion stateRegion = new StateRegion();
        stateRegion.setObjectType(stateRegionEntity.getObjectType());
        stateRegion.setName(stateRegionEntity.getName());
        stateRegion.setSearchRegion(regionEmbeddableMapper.map(stateRegionEntity.getSearchRegion()));
        stateRegion.setOwnerStateName(stateRegionEntity.getOwnerStateName());
        stateRegion.setStaysVisibleAfterClicked(stateRegionEntity.getStaysVisibleAfterClicked());
        stateRegion.setProbabilityExists(stateRegionEntity.getProbabilityExists());
        stateRegion.setTimesActedOn(stateRegionEntity.getTimesActedOn());
        stateRegion.setPosition(positionEmbeddableMapper.map(stateRegionEntity.getPosition()));
        stateRegion.setAnchors(anchorsEntityMapper.map(stateRegionEntity.getAnchors()));
        stateRegion.setMockText(stateRegionEntity.getMockText());
        stateRegion.setMatchHistory(matchHistoryEntityMapper.map(stateRegionEntity.getMatchHistory()));
        return stateRegion;
    }

    public Set<StateRegionEntity> mapToStateRegionEntitySet(Set<StateRegion> stateRegions) {
        return new HashSet<>(mapToStateRegionEntityList(stateRegions.stream().toList()));
    }

    public List<StateRegionEntity> mapToStateRegionEntityList(List<StateRegion> stateRegions) {
        List<StateRegionEntity> stateRegionEntityList = new ArrayList<>();
        stateRegions.forEach(stateRegion -> stateRegionEntityList.add(map(stateRegion)));
        return stateRegionEntityList;
    }

    public Set<StateRegion> mapToStateRegionSet(Set<StateRegionEntity> stateRegionEntities) {
        return new HashSet<>(mapToStateRegionList(stateRegionEntities.stream().toList()));
    }

    public List<StateRegion> mapToStateRegionList(List<StateRegionEntity> stateRegionEntities) {
        List<StateRegion> stateRegionList = new ArrayList<>();
        stateRegionEntities.forEach(stateRegionEntity -> stateRegionList.add(map(stateRegionEntity)));
        return stateRegionList;
    }
    
}
