package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.StateStringEntity;
import io.github.jspinak.brobot.app.web.responses.StateStringResponse;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StateStringResponseMapper {

    private final RegionResponseMapper regionResponseMapper;

    public StateStringResponseMapper(RegionResponseMapper regionResponseMapper) {
        this.regionResponseMapper = regionResponseMapper;
    }

    public StateStringResponse map(StateStringEntity stateStringEntity) {
        if (stateStringEntity == null) {
            return null;
        }
        StateStringResponse response = new StateStringResponse();
        response.setObjectType(stateStringEntity.getObjectType());
        response.setName(stateStringEntity.getName());
        response.setSearchRegion(regionResponseMapper.map(stateStringEntity.getSearchRegion()));
        response.setOwnerStateName(stateStringEntity.getOwnerStateName());
        response.setTimesActedOn(stateStringEntity.getTimesActedOn());
        response.setString(stateStringEntity.getString());
        return response;
    }

    public StateStringEntity map(StateStringResponse response) {
        if (response == null) {
            return null;
        }
        StateStringEntity stateStringEntity = new StateStringEntity();
        stateStringEntity.setObjectType(response.getObjectType());
        stateStringEntity.setName(response.getName());
        stateStringEntity.setSearchRegion(regionResponseMapper.map(response.getSearchRegion()));
        stateStringEntity.setOwnerStateName(response.getOwnerStateName());
        stateStringEntity.setTimesActedOn(response.getTimesActedOn());
        stateStringEntity.setString(response.getString());
        return stateStringEntity;
    }

    public List<StateStringResponse> mapToListResponse(List<StateStringEntity> stateStringEntities) {
        if (stateStringEntities == null) {
            return null;
        }
        return stateStringEntities.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public List<StateStringEntity> mapToListEntity(List<StateStringResponse> stateStringResponses) {
        if (stateStringResponses == null) {
            return null;
        }
        return stateStringResponses.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}

