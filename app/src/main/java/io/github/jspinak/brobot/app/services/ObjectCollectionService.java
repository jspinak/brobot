package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.ObjectCollectionEntityMapper;
import io.github.jspinak.brobot.app.database.entities.ObjectCollectionEntity;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Service;

@Service
public class ObjectCollectionService {

    private final ObjectCollectionEntityMapper objectCollectionEntityMapper;
    private final StateImageService stateImageService;
    private final SceneService sceneService;
    private final PatternService patternService;

    public ObjectCollectionService(ObjectCollectionEntityMapper objectCollectionEntityMapper,
                                   StateImageService stateImageService, SceneService sceneService,
                                   PatternService patternService) {
        this.objectCollectionEntityMapper = objectCollectionEntityMapper;
        this.stateImageService = stateImageService;
        this.sceneService = sceneService;
        this.patternService = patternService;
    }

    // TODO: map scenes
    public ObjectCollection mapObjectCollection(ObjectCollectionEntity entity) {
        ObjectCollection objectCollection = objectCollectionEntityMapper.map(entity, sceneService, patternService);
        objectCollection.setStateImages(stateImageService.mapWithImages(entity.getStateImages()));
        return objectCollection;
    }
}
