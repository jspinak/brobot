package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.ObjectCollectionEntity;
import io.github.jspinak.brobot.app.web.requests.ObjectCollectionRequest;
import io.github.jspinak.brobot.app.web.responses.ObjectCollectionResponse;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ObjectCollectionResponseMapper {

    private final StateLocationResponseMapper stateLocationResponseMapper;
    private final StateImageResponseMapper stateImageResponseMapper;
    private final StateRegionResponseMapper stateRegionResponseMapper;
    private final StateStringResponseMapper stateStringResponseMapper;
    private final MatchesResponseMapper matchesResponseMapper;
    private final SceneResponseMapper sceneResponseMapper;

    public ObjectCollectionResponseMapper(StateLocationResponseMapper stateLocationResponseMapper,
                                          StateImageResponseMapper stateImageResponseMapper,
                                          StateRegionResponseMapper stateRegionResponseMapper,
                                          StateStringResponseMapper stateStringResponseMapper,
                                          MatchesResponseMapper matchesResponseMapper,
                                          SceneResponseMapper sceneResponseMapper) {
        this.stateLocationResponseMapper = stateLocationResponseMapper;
        this.stateImageResponseMapper = stateImageResponseMapper;
        this.stateRegionResponseMapper = stateRegionResponseMapper;
        this.stateStringResponseMapper = stateStringResponseMapper;
        this.matchesResponseMapper = matchesResponseMapper;
        this.sceneResponseMapper = sceneResponseMapper;
    }

    public ObjectCollectionResponse toResponse(ObjectCollectionEntity entity) {
        if (entity == null) {
            return null;
        }
        ObjectCollectionResponse response = new ObjectCollectionResponse();
        response.setId(entity.getId());
        response.setStateLocations(entity.getStateLocations());
        response.setStateImages(entity.getStateImages());
        response.setStateRegions(entity.getStateRegions());
        response.setStateStrings(entity.getStateStrings());
        response.setMatches(entity.getMatches());
        response.setScenes(entity.getScenes());
        return response;
    }

    public ObjectCollectionEntity toEntity(ObjectCollectionResponse response) {
        if (response == null) {
            return null;
        }
        ObjectCollectionEntity entity = new ObjectCollectionEntity();
        entity.setId(response.getId());
        entity.setStateLocations(response.getStateLocations());
        entity.setStateImages(response.getStateImages());
        entity.setStateRegions(response.getStateRegions());
        entity.setStateStrings(response.getStateStrings());
        entity.setMatches(response.getMatches());
        entity.setScenes(response.getScenes());
        return entity;
    }

    public ObjectCollectionEntity fromRequest(ObjectCollectionRequest request) {
        if (request == null) return null;
        ObjectCollectionEntity entity = new ObjectCollectionEntity();
        entity.setId(request.getId());
        entity.setStateLocations(request.getStateLocations().stream()
                .map(stateLocationResponseMapper::fromRequest)
                .collect(Collectors.toList()));
        entity.setStateImages(request.getStateImages().stream()
                .map(stateImageResponseMapper::fromRequest)
                .collect(Collectors.toList()));
        entity.setStateRegions(request.getStateRegions().stream()
                .map(stateRegionResponseMapper::fromRequest)
                .collect(Collectors.toList()));
        entity.setStateStrings(request.getStateStrings().stream()
                .map(stateStringResponseMapper::fromRequest)
                .collect(Collectors.toList()));
        entity.setMatches(request.getMatches().stream()
                .map(matchesResponseMapper::fromRequest)
                .collect(Collectors.toList()));
        entity.setScenes(request.getScenes().stream()
                .map(sceneResponseMapper::fromRequest)
                .collect(Collectors.toList()));
        return entity;
    }

    public ObjectCollectionRequest toRequest(ObjectCollectionEntity entity) {
        if (entity == null) {
            return null;
        }

        ObjectCollectionRequest request = new ObjectCollectionRequest();
        request.setId(entity.getId());
        request.setStateLocations(entity.getStateLocations().stream()
                .map(stateLocationResponseMapper::toRequest)
                .collect(Collectors.toList()));
        request.setStateImages(entity.getStateImages().stream()
                .map(stateImageResponseMapper::toRequest)
                .collect(Collectors.toList()));
        request.setStateRegions(entity.getStateRegions().stream()
                .map(stateRegionResponseMapper::toRequest)
                .collect(Collectors.toList()));
        request.setStateStrings(entity.getStateStrings().stream()
                .map(stateStringResponseMapper::toRequest)
                .collect(Collectors.toList()));
        request.setMatches(entity.getMatches().stream()
                .map(matchesResponseMapper::toRequest)
                .collect(Collectors.toList()));
        request.setScenes(entity.getScenes().stream()
                .map(sceneResponseMapper::toRequest)
                .collect(Collectors.toList()));

        return request;
    }
}
