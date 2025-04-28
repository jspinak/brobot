package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.ObjectCollectionEntity;
import io.github.jspinak.brobot.app.services.PatternService;
import io.github.jspinak.brobot.app.services.SceneService;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

@Component
public class ObjectCollectionEntityMapper {

    private final StateLocationEntityMapper stateLocationEntityMapper;
    private final StateImageEntityMapper stateImageEntityMapper;
    private final StateRegionEntityMapper stateRegionEntityMapper;
    private final StateStringEntityMapper stateStringEntityMapper;
    private final MatchesEntityMapper matchesEntityMapper;

    public ObjectCollectionEntityMapper(StateLocationEntityMapper stateLocationEntityMapper,
                                        StateImageEntityMapper stateImageEntityMapper,
                                        StateRegionEntityMapper stateRegionEntityMapper,
                                        StateStringEntityMapper stateStringEntityMapper,
                                        MatchesEntityMapper matchesEntityMapper) {
        this.stateLocationEntityMapper = stateLocationEntityMapper;
        this.stateImageEntityMapper = stateImageEntityMapper;
        this.stateRegionEntityMapper = stateRegionEntityMapper;
        this.stateStringEntityMapper = stateStringEntityMapper;
        this.matchesEntityMapper = matchesEntityMapper;
    }
    
    public ObjectCollectionEntity map(ObjectCollection objectCollection, SceneService sceneService, PatternService patternService) {
        ObjectCollectionEntity objectCollectionEntity = new ObjectCollectionEntity();
        objectCollectionEntity.setStateLocations(stateLocationEntityMapper.mapToStateLocationEntityList(objectCollection.getStateLocations()));
        objectCollectionEntity.setStateImages(stateImageEntityMapper.mapToStateImageEntityList(objectCollection.getStateImages(), patternService));
        objectCollectionEntity.setStateRegions(stateRegionEntityMapper.mapToStateRegionEntityList(objectCollection.getStateRegions()));
        objectCollectionEntity.setStateStrings(stateStringEntityMapper.mapToStateStringEntityList(objectCollection.getStateStrings()));
        objectCollectionEntity.setMatches(matchesEntityMapper.mapToMatchesEntityList(objectCollection.getMatches()));
        objectCollectionEntity.setScenes(sceneService.mapToSceneEntityList((objectCollection.getScenes())));
        return objectCollectionEntity;
    }

    public ObjectCollection map(ObjectCollectionEntity objectCollectionEntity, SceneService sceneService, PatternService patternService) {
        ObjectCollection objectCollection = new ObjectCollection();
        objectCollection.setStateLocations(stateLocationEntityMapper.mapToStateLocationList(objectCollectionEntity.getStateLocations()));
        objectCollection.setStateImages(stateImageEntityMapper.mapToStateImageList(objectCollectionEntity.getStateImages(), patternService));
        objectCollection.setStateRegions(stateRegionEntityMapper.mapToStateRegionList(objectCollectionEntity.getStateRegions()));
        objectCollection.setStateStrings(stateStringEntityMapper.mapToStateStringList(objectCollectionEntity.getStateStrings()));
        objectCollection.setMatches(matchesEntityMapper.mapToMatchesList(objectCollectionEntity.getMatches()));
        objectCollection.setScenes(sceneService.mapToSceneList(objectCollectionEntity.getScenes()));
        return objectCollection;
    }
}
