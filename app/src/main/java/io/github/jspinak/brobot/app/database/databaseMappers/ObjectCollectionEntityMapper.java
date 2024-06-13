package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.ObjectCollectionEntity;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;

public class ObjectCollectionEntityMapper {
    
    public static ObjectCollectionEntity map(ObjectCollection objectCollection) {
        ObjectCollectionEntity objectCollectionEntity = new ObjectCollectionEntity();
        objectCollectionEntity.setStateLocations(StateLocationEntityMapper.mapToStateLocationEntityList(objectCollection.getStateLocations()));
        objectCollectionEntity.setStateImages(StateImageEntityMapper.mapToStateImageEntityList(objectCollection.getStateImages()));
        objectCollectionEntity.setStateRegions(StateRegionEntityMapper.mapToStateRegionEntityList(objectCollection.getStateRegions()));
        objectCollectionEntity.setStateStrings(StateStringEntityMapper.mapToStateStringEntityList(objectCollection.getStateStrings()));
        objectCollectionEntity.setMatches(MatchesEntityMapper.mapToMatchesEntityList(objectCollection.getMatches()));
        objectCollectionEntity.setScenes(PatternEntityMapper.mapToPatternEntityList(objectCollection.getScenes()));
        return objectCollectionEntity;
    }

    public static ObjectCollection map(ObjectCollectionEntity objectCollectionEntity) {
        ObjectCollection objectCollection = new ObjectCollection();
        objectCollection.setStateLocations(StateLocationEntityMapper.mapToStateLocationList(objectCollectionEntity.getStateLocations()));
        objectCollection.setStateImages(StateImageEntityMapper.mapToStateImageList(objectCollectionEntity.getStateImages()));
        objectCollection.setStateRegions(StateRegionEntityMapper.mapToStateRegionList(objectCollectionEntity.getStateRegions()));
        objectCollection.setStateStrings(StateStringEntityMapper.mapToStateStringList(objectCollectionEntity.getStateStrings()));
        objectCollection.setMatches(MatchesEntityMapper.mapToMatchesList(objectCollectionEntity.getMatches()));
        objectCollection.setScenes(PatternEntityMapper.mapToPatternList(objectCollectionEntity.getScenes()));
        return objectCollection;
    }
}
