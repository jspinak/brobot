package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.StateRegionEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StateRegionEntityMapper {
    
    public static StateRegionEntity map(StateRegion stateRegion) {
        StateRegionEntity stateRegionEntity = new StateRegionEntity();
        stateRegionEntity.setObjectType(stateRegion.getObjectType());
        stateRegionEntity.setName(stateRegion.getName());
        stateRegionEntity.setSearchRegion(RegionEmbeddableMapper.map(stateRegion.getSearchRegion()));
        stateRegionEntity.setOwnerStateName(stateRegion.getOwnerStateName());
        stateRegionEntity.setStaysVisibleAfterClicked(stateRegion.getStaysVisibleAfterClicked());
        stateRegionEntity.setProbabilityExists(stateRegion.getProbabilityExists());
        stateRegionEntity.setTimesActedOn(stateRegion.getTimesActedOn());
        stateRegionEntity.setPosition(PositionEmbeddableMapper.map(stateRegion.getPosition()));
        stateRegionEntity.setAnchors(AnchorsEntityMapper.map(stateRegion.getAnchors()));
        stateRegionEntity.setMockText(stateRegion.getMockText());
        stateRegionEntity.setMatchHistory(MatchHistoryEntityMapper.map(stateRegion.getMatchHistory()));
        return stateRegionEntity;
    }
    public static StateRegion map(StateRegionEntity stateRegionEntity) {
        StateRegion stateRegion = new StateRegion();
        stateRegion.setObjectType(stateRegionEntity.getObjectType());
        stateRegion.setName(stateRegionEntity.getName());
        stateRegion.setSearchRegion(RegionEmbeddableMapper.map(stateRegionEntity.getSearchRegion()));
        stateRegion.setOwnerStateName(stateRegionEntity.getOwnerStateName());
        stateRegion.setStaysVisibleAfterClicked(stateRegionEntity.getStaysVisibleAfterClicked());
        stateRegion.setProbabilityExists(stateRegionEntity.getProbabilityExists());
        stateRegion.setTimesActedOn(stateRegionEntity.getTimesActedOn());
        stateRegion.setPosition(PositionEmbeddableMapper.map(stateRegionEntity.getPosition()));
        stateRegion.setAnchors(AnchorsEntityMapper.map(stateRegionEntity.getAnchors()));
        stateRegion.setMockText(stateRegionEntity.getMockText());
        stateRegion.setMatchHistory(MatchHistoryEntityMapper.map(stateRegionEntity.getMatchHistory()));
        return stateRegion;
    }

    public static Set<StateRegionEntity> mapToStateRegionEntitySet(Set<StateRegion> stateRegions) {
        return new HashSet<>(mapToStateRegionEntityList(stateRegions.stream().toList()));
    }

    public static List<StateRegionEntity> mapToStateRegionEntityList(List<StateRegion> stateRegions) {
        List<StateRegionEntity> stateRegionEntityList = new ArrayList<>();
        stateRegions.forEach(stateRegion -> stateRegionEntityList.add(map(stateRegion)));
        return stateRegionEntityList;
    }

    public static Set<StateRegion> mapToStateRegionSet(Set<StateRegionEntity> stateRegionEntities) {
        return new HashSet<>(mapToStateRegionList(stateRegionEntities.stream().toList()));
    }

    public static List<StateRegion> mapToStateRegionList(List<StateRegionEntity> stateRegionEntities) {
        List<StateRegion> stateRegionList = new ArrayList<>();
        stateRegionEntities.forEach(stateRegionEntity -> stateRegionList.add(map(stateRegionEntity)));
        return stateRegionList;
    }
    
}
