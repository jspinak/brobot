package io.github.jspinak.brobot.app.web.responseMappers;

import io.github.jspinak.brobot.app.database.entities.StateRegionEntity;
import io.github.jspinak.brobot.app.web.responses.StateRegionResponse;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StateRegionResponseMapper {

    private final RegionResponseMapper regionResponseMapper;
    private final PositionResponseMapper positionResponseMapper;
    private final AnchorsResponseMapper anchorsResponseMapper;
    private final MatchHistoryResponseMapper matchHistoryResponseMapper;

    public StateRegionResponseMapper(RegionResponseMapper regionResponseMapper,
                                     PositionResponseMapper positionResponseMapper,
                                     AnchorsResponseMapper anchorsResponseMapper,
                                     MatchHistoryResponseMapper matchHistoryResponseMapper) {
        this.regionResponseMapper = regionResponseMapper;
        this.positionResponseMapper = positionResponseMapper;
        this.anchorsResponseMapper = anchorsResponseMapper;
        this.matchHistoryResponseMapper = matchHistoryResponseMapper;
    }

    public StateRegionResponse map(StateRegionEntity stateRegionEntity) {
        if (stateRegionEntity == null) {
            return null;
        }
        StateRegionResponse response = new StateRegionResponse();
        response.setObjectType(stateRegionEntity.getObjectType());
        response.setName(stateRegionEntity.getName());
        response.setSearchRegion(regionResponseMapper.map(stateRegionEntity.getSearchRegion()));
        response.setOwnerStateName(stateRegionEntity.getOwnerStateName());
        response.setStaysVisibleAfterClicked(stateRegionEntity.getStaysVisibleAfterClicked());
        response.setProbabilityExists(stateRegionEntity.getProbabilityExists());
        response.setTimesActedOn(stateRegionEntity.getTimesActedOn());
        response.setPosition(positionResponseMapper.map(stateRegionEntity.getPosition()));
        response.setAnchors(anchorsResponseMapper.map(stateRegionEntity.getAnchors()));
        response.setMockText(stateRegionEntity.getMockText());
        response.setMatchHistory(matchHistoryResponseMapper.map(stateRegionEntity.getMatchHistory()));
        return response;
    }

    public StateRegionEntity map(StateRegionResponse response) {
        if (response == null) {
            return null;
        }
        StateRegionEntity stateRegionEntity = new StateRegionEntity();
        stateRegionEntity.setObjectType(response.getObjectType());
        stateRegionEntity.setName(response.getName());
        stateRegionEntity.setSearchRegion(regionResponseMapper.map(response.getSearchRegion()));
        stateRegionEntity.setOwnerStateName(response.getOwnerStateName());
        stateRegionEntity.setStaysVisibleAfterClicked(response.getStaysVisibleAfterClicked());
        stateRegionEntity.setProbabilityExists(response.getProbabilityExists());
        stateRegionEntity.setTimesActedOn(response.getTimesActedOn());
        stateRegionEntity.setPosition(positionResponseMapper.map(response.getPosition()));
        stateRegionEntity.setAnchors(anchorsResponseMapper.map(response.getAnchors()));
        stateRegionEntity.setMockText(response.getMockText());
        stateRegionEntity.setMatchHistory(matchHistoryResponseMapper.map(response.getMatchHistory()));
        return stateRegionEntity;
    }

    public List<StateRegionResponse> mapToListResponse(List<StateRegionEntity> stateRegionEntities) {
        if (stateRegionEntities == null) {
            return null;
        }
        return stateRegionEntities.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public List<StateRegionEntity> mapToListEntity(List<StateRegionResponse> stateRegionResponses) {
        if (stateRegionResponses == null) {
            return null;
        }
        return stateRegionResponses.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}

