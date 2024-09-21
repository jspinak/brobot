package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.database.databaseMappers.ObjectCollectionEntityMapper;
import io.github.jspinak.brobot.app.database.entities.ObjectCollectionEntity;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Service;

@Service
public class ObjectCollectionService {

    private final ObjectCollectionEntityMapper objectCollectionEntityMapper;
    private final StateImageService stateImageService;

    public ObjectCollectionService(ObjectCollectionEntityMapper objectCollectionEntityMapper,
                                   StateImageService stateImageService) {
        this.objectCollectionEntityMapper = objectCollectionEntityMapper;
        this.stateImageService = stateImageService;
    }

    // TODO: map scenes
    public ObjectCollection mapObjectCollection(ObjectCollectionEntity entity) {
        ObjectCollection objectCollection = objectCollectionEntityMapper.mapWithoutStateImagesAndScenes(entity);
        objectCollection.setStateImages(stateImageService.mapWithImages(entity.getStateImages()));
        return objectCollection;
    }
}
