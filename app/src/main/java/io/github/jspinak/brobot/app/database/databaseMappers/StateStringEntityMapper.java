package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.StateStringEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class StateStringEntityMapper {

    private RegionEmbeddableMapper regionEmbeddableMapper;

    public StateStringEntityMapper(RegionEmbeddableMapper regionEmbeddableMapper) {
        this.regionEmbeddableMapper = regionEmbeddableMapper;
    }
    
    public StateStringEntity map(StateString stateString) {
        StateStringEntity stateStringEntity = new StateStringEntity();
        stateStringEntity.setObjectType(stateString.getObjectType());
        stateStringEntity.setName(stateString.getName());
        stateStringEntity.setSearchRegion(regionEmbeddableMapper.map(stateString.getSearchRegion()));
        stateStringEntity.setOwnerStateName(stateString.getOwnerStateName());
        stateStringEntity.setTimesActedOn(stateString.getTimesActedOn());
        stateStringEntity.setString(stateString.getString());
        return stateStringEntity;
    }

    public StateString map(StateStringEntity stateStringEntity) {
        StateString stateString = new StateString();
        stateString.setObjectType(stateStringEntity.getObjectType());
        stateString.setName(stateStringEntity.getName());
        stateString.setSearchRegion(regionEmbeddableMapper.map(stateStringEntity.getSearchRegion()));
        stateString.setOwnerStateName(stateStringEntity.getOwnerStateName());
        stateString.setTimesActedOn(stateStringEntity.getTimesActedOn());
        stateString.setString(stateStringEntity.getString());
        return stateString;
    }

    public Set<StateStringEntity> mapToStateStringEntitySet(Set<StateString> stateStrings) {
        return new HashSet<>(mapToStateStringEntityList(stateStrings.stream().toList()));
    }

    public List<StateStringEntity> mapToStateStringEntityList(List<StateString> stateStrings) {
        List<StateStringEntity> stateStringEntityList = new ArrayList<>();
        stateStrings.forEach(stateString -> stateStringEntityList.add(map(stateString)));
        return stateStringEntityList;
    }

    public Set<StateString> mapToStateStringSet(Set<StateStringEntity> stateStringEntities) {
        return new HashSet<>(mapToStateStringList(stateStringEntities.stream().toList()));
    }

    public List<StateString> mapToStateStringList(List<StateStringEntity> stateStringEntities) {
        List<StateString> stateStringList = new ArrayList<>();
        stateStringEntities.forEach(stateStringEntity -> stateStringList.add(map(stateStringEntity)));
        return stateStringList;
    }
}
