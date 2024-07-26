package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.StateLocationEntity;
import io.github.jspinak.brobot.app.web.responses.StateLocationResponse;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StateLocationResponseMapper {

    private final LocationResponseMapper locationResponseMapper;
    private final PositionResponseMapper positionResponseMapper;
    private final AnchorsResponseMapper anchorsResponseMapper;
    private final MatchHistoryResponseMapper matchHistoryResponseMapper;

    public StateLocationResponseMapper(LocationResponseMapper locationResponseMapper,
                                       PositionResponseMapper positionResponseMapper,
                                       AnchorsResponseMapper anchorsResponseMapper,
                                       MatchHistoryResponseMapper matchHistoryResponseMapper) {
        this.locationResponseMapper = locationResponseMapper;
        this.positionResponseMapper = positionResponseMapper;
        this.anchorsResponseMapper = anchorsResponseMapper;
        this.matchHistoryResponseMapper = matchHistoryResponseMapper;
    }

    public StateLocationResponse map(StateLocationEntity stateLocationEntity) {
        if (stateLocationEntity == null) {
            return null;
        }
        StateLocationResponse response = new StateLocationResponse();
        response.setObjectType(stateLocationEntity.getObjectType());
        response.setName(stateLocationEntity.getName());
        response.setLocation(locationResponseMapper.map(stateLocationEntity.getLocation()));
        response.setOwnerStateName(stateLocationEntity.getOwnerStateName());
        response.setStaysVisibleAfterClicked(stateLocationEntity.getStaysVisibleAfterClicked());
        response.setProbabilityExists(stateLocationEntity.getProbabilityExists());
        response.setTimesActedOn(stateLocationEntity.getTimesActedOn());
        response.setPosition(positionResponseMapper.map(stateLocationEntity.getPosition()));
        response.setAnchors(anchorsResponseMapper.map(stateLocationEntity.getAnchors()));
        response.setMatchHistory(matchHistoryResponseMapper.map(stateLocationEntity.getMatchHistory()));
        return response;
    }

    public StateLocationEntity map(StateLocationResponse response) {
        if (response == null) {
            return null;
        }
        StateLocationEntity stateLocationEntity = new StateLocationEntity();
        stateLocationEntity.setObjectType(response.getObjectType());
        stateLocationEntity.setName(response.getName());
        stateLocationEntity.setLocation(locationResponseMapper.map(response.getLocation()));
        stateLocationEntity.setOwnerStateName(response.getOwnerStateName());
        stateLocationEntity.setStaysVisibleAfterClicked(response.getStaysVisibleAfterClicked());
        stateLocationEntity.setProbabilityExists(response.getProbabilityExists());
        stateLocationEntity.setTimesActedOn(response.getTimesActedOn());
        stateLocationEntity.setPosition(positionResponseMapper.map(response.getPosition()));
        stateLocationEntity.setAnchors(anchorsResponseMapper.map(response.getAnchors()));
        stateLocationEntity.setMatchHistory(matchHistoryResponseMapper.map(response.getMatchHistory()));
        return stateLocationEntity;
    }

    public List<StateLocationResponse> mapToListResponse(List<StateLocationEntity> stateLocationEntities) {
        if (stateLocationEntities == null) {
            return null;
        }
        return stateLocationEntities.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public List<StateLocationEntity> mapToListEntity(List<StateLocationResponse> stateLocationResponses) {
        if (stateLocationResponses == null) {
            return null;
        }
        return stateLocationResponses.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}

