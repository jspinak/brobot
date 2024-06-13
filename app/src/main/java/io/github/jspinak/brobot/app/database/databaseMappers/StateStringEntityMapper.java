package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.StateStringEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StateStringEntityMapper {
    
    public static StateStringEntity map(StateString stateString) {
        StateStringEntity stateStringEntity = new StateStringEntity();
        stateStringEntity.setObjectType(stateString.getObjectType());
        stateStringEntity.setName(stateString.getName());
        stateStringEntity.setSearchRegion(RegionEmbeddableMapper.map(stateString.getSearchRegion()));
        stateStringEntity.setOwnerStateName(stateString.getOwnerStateName());
        stateStringEntity.setTimesActedOn(stateString.getTimesActedOn());
        stateStringEntity.setString(stateString.getString());
        return stateStringEntity;
    }

    public static StateString map(StateStringEntity stateStringEntity) {
        StateString stateString = new StateString();
        stateString.setObjectType(stateStringEntity.getObjectType());
        stateString.setName(stateStringEntity.getName());
        stateString.setSearchRegion(RegionEmbeddableMapper.map(stateStringEntity.getSearchRegion()));
        stateString.setOwnerStateName(stateStringEntity.getOwnerStateName());
        stateString.setTimesActedOn(stateStringEntity.getTimesActedOn());
        stateString.setString(stateStringEntity.getString());
        return stateString;
    }

    public static Set<StateStringEntity> mapToStateStringEntitySet(Set<StateString> stateStrings) {
        return new HashSet<>(mapToStateStringEntityList(stateStrings.stream().toList()));
    }

    public static List<StateStringEntity> mapToStateStringEntityList(List<StateString> stateStrings) {
        List<StateStringEntity> stateStringEntityList = new ArrayList<>();
        stateStrings.forEach(stateString -> stateStringEntityList.add(map(stateString)));
        return stateStringEntityList;
    }

    public static Set<StateString> mapToStateStringSet(Set<StateStringEntity> stateStringEntities) {
        return new HashSet<>(mapToStateStringList(stateStringEntities.stream().toList()));
    }

    public static List<StateString> mapToStateStringList(List<StateStringEntity> stateStringEntities) {
        List<StateString> stateStringList = new ArrayList<>();
        stateStringEntities.forEach(stateStringEntity -> stateStringList.add(map(stateStringEntity)));
        return stateStringList;
    }
}
