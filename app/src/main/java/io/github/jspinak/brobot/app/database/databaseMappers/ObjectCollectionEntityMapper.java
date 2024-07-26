package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.ObjectCollectionEntity;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

@Component
public class ObjectCollectionEntityMapper {

    private final StateLocationEntityMapper stateLocationEntityMapper;
    private final StateImageEntityMapper stateImageEntityMapper;
    private final StateRegionEntityMapper stateRegionEntityMapper;
    private final StateStringEntityMapper stateStringEntityMapper;
    private final MatchesEntityMapper matchesEntityMapper;
    private final PatternEntityMapper patternEntityMapper;

    public ObjectCollectionEntityMapper(StateLocationEntityMapper stateLocationEntityMapper,
                                        StateImageEntityMapper stateImageEntityMapper,
                                        StateRegionEntityMapper stateRegionEntityMapper,
                                        StateStringEntityMapper stateStringEntityMapper,
                                        MatchesEntityMapper matchesEntityMapper,
                                        PatternEntityMapper patternEntityMapper) {
        this.stateLocationEntityMapper = stateLocationEntityMapper;
        this.stateImageEntityMapper = stateImageEntityMapper;
        this.stateRegionEntityMapper = stateRegionEntityMapper;
        this.stateStringEntityMapper = stateStringEntityMapper;
        this.matchesEntityMapper = matchesEntityMapper;
        this.patternEntityMapper = patternEntityMapper;
    }
    
    public ObjectCollectionEntity map(ObjectCollection objectCollection) {
        ObjectCollectionEntity objectCollectionEntity = new ObjectCollectionEntity();
        objectCollectionEntity.setStateLocations(stateLocationEntityMapper.mapToStateLocationEntityList(objectCollection.getStateLocations()));
        objectCollectionEntity.setStateImages(stateImageEntityMapper.mapToStateImageEntityList(objectCollection.getStateImages()));
        objectCollectionEntity.setStateRegions(stateRegionEntityMapper.mapToStateRegionEntityList(objectCollection.getStateRegions()));
        objectCollectionEntity.setStateStrings(stateStringEntityMapper.mapToStateStringEntityList(objectCollection.getStateStrings()));
        objectCollectionEntity.setMatches(matchesEntityMapper.mapToMatchesEntityList(objectCollection.getMatches()));
        objectCollectionEntity.setScenes(patternEntityMapper.mapToPatternEntityList(objectCollection.getScenes()));
        return objectCollectionEntity;
    }

    public ObjectCollection map(ObjectCollectionEntity objectCollectionEntity) {
        ObjectCollection objectCollection = new ObjectCollection();
        objectCollection.setStateLocations(stateLocationEntityMapper.mapToStateLocationList(objectCollectionEntity.getStateLocations()));
        objectCollection.setStateImages(stateImageEntityMapper.mapToStateImageList(objectCollectionEntity.getStateImages()));
        objectCollection.setStateRegions(stateRegionEntityMapper.mapToStateRegionList(objectCollectionEntity.getStateRegions()));
        objectCollection.setStateStrings(stateStringEntityMapper.mapToStateStringList(objectCollectionEntity.getStateStrings()));
        objectCollection.setMatches(matchesEntityMapper.mapToMatchesList(objectCollectionEntity.getMatches()));
        objectCollection.setScenes(patternEntityMapper.mapToPatternList(objectCollectionEntity.getScenes()));
        return objectCollection;
    }
}
