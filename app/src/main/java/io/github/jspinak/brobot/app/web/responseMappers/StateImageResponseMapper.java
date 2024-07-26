package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.app.web.responses.StateImageResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StateImageResponseMapper {

    private final PatternResponseMapper patternResponseMapper;

    public StateImageResponseMapper(PatternResponseMapper patternResponseMapper) {
        this.patternResponseMapper = patternResponseMapper;
    }

    public StateImageResponse map(StateImageEntity stateImageEntity) {
        if (stateImageEntity == null) {
            return null;
        }
        StateImageResponse response = new StateImageResponse();
        response.setId(stateImageEntity.getId());
        response.setProjectId(stateImageEntity.getProjectId());
        response.setObjectType(stateImageEntity.getObjectType());
        response.setName(stateImageEntity.getName());
        response.setPatterns(stateImageEntity.getPatterns().stream()
                .map(patternResponseMapper::map)
                .collect(Collectors.toList()));
        response.setOwnerStateName(stateImageEntity.getOwnerStateName());
        response.setTimesActedOn(stateImageEntity.getTimesActedOn());
        response.setShared(stateImageEntity.isShared());
        response.setIndex(stateImageEntity.getIndex());
        response.setDynamic(stateImageEntity.isDynamic());
        return response;
    }

    public StateImageEntity map(StateImageResponse response) {
        if (response == null) {
            return null;
        }
        StateImageEntity stateImageEntity = new StateImageEntity();
        stateImageEntity.setId(response.getId());
        stateImageEntity.setProjectId(response.getProjectId());
        stateImageEntity.setObjectType(response.getObjectType());
        stateImageEntity.setName(response.getName());
        stateImageEntity.setPatterns(response.getPatterns().stream()
                .map(patternResponseMapper::map)
                .collect(Collectors.toList()));
        stateImageEntity.setOwnerStateName(response.getOwnerStateName());
        stateImageEntity.setTimesActedOn(response.getTimesActedOn());
        stateImageEntity.setShared(response.isShared());
        stateImageEntity.setIndex(response.getIndex());
        stateImageEntity.setDynamic(response.isDynamic());
        return stateImageEntity;
    }

    public List<StateImageResponse> mapToListResponse(List<StateImageEntity> stateImageEntities) {
        if (stateImageEntities == null) {
            return null;
        }
        return stateImageEntities.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public List<StateImageEntity> mapToListEntity(List<StateImageResponse> stateImageResponses) {
        if (stateImageResponses == null) {
            return null;
        }
        return stateImageResponses.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}

