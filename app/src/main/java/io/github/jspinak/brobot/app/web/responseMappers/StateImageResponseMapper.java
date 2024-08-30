package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.app.web.requests.StateImageRequest;
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
        response.setOwnerStateId(stateImageEntity.getOwnerStateId());
        response.setOwnerStateName(stateImageEntity.getOwnerStateName());
        response.setTimesActedOn(stateImageEntity.getTimesActedOn());
        response.setShared(stateImageEntity.isShared());
        response.setIndex(stateImageEntity.getIndex());
        response.setDynamic(stateImageEntity.isDynamic());
        response.setInvolvedTransitionIds(stateImageEntity.getInvolvedTransitionIds());
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
        stateImageEntity.setOwnerStateId(response.getOwnerStateId());
        stateImageEntity.setOwnerStateName(response.getOwnerStateName());
        stateImageEntity.setTimesActedOn(response.getTimesActedOn());
        stateImageEntity.setShared(response.isShared());
        stateImageEntity.setIndex(response.getIndex());
        stateImageEntity.setDynamic(response.isDynamic());
        stateImageEntity.setInvolvedTransitionIds(response.getInvolvedTransitionIds());
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

    public StateImageEntity fromRequest(StateImageRequest request) {
        if (request == null) return null;
        StateImageEntity entity = new StateImageEntity();
        entity.setId(request.getId());
        entity.setProjectId(request.getProjectId());
        entity.setObjectType(request.getObjectType());
        entity.setName(request.getName());
        entity.setPatterns(request.getPatterns().stream()
                .map(patternResponseMapper::fromRequest)
                .collect(Collectors.toList()));
        entity.setOwnerStateId(request.getOwnerStateId());
        entity.setOwnerStateName(request.getOwnerStateName());
        entity.setTimesActedOn(request.getTimesActedOn());
        entity.setShared(request.isShared());
        entity.setIndex(request.getIndex());
        entity.setDynamic(request.isDynamic());
        entity.setInvolvedTransitionIds(request.getInvolvedTransitionIds());
        return entity;
    }

    public StateImageRequest toRequest(StateImageEntity entity) {
        if (entity == null) {
            return null;
        }
        StateImageRequest request = new StateImageRequest();
        request.setId(entity.getId());
        request.setProjectId(entity.getProjectId());
        request.setObjectType(entity.getObjectType());
        request.setName(entity.getName());
        request.setPatterns(entity.getPatterns().stream()
                .map(patternResponseMapper::toRequest)
                .collect(Collectors.toList()));
        request.setOwnerStateId(entity.getOwnerStateId());
        request.setOwnerStateName(entity.getOwnerStateName());
        request.setTimesActedOn(entity.getTimesActedOn());
        request.setShared(entity.isShared());
        request.setIndex(entity.getIndex());
        request.setDynamic(entity.isDynamic());
        request.setInvolvedTransitionIds(entity.getInvolvedTransitionIds());
        return request;
    }

    public List<StateImageRequest> toRequestList(List<StateImageEntity> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toRequest)
                .collect(Collectors.toList());
    }
}

