package com.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface StateRegionResponseMapper {

    StateRegionResponseMapper INSTANCE = Mappers.getMapper(StateRegionResponseMapper.class);

    @Mapping(source = "Region", target = "RegionResponse")
    @Mapping(source = "Position", target = "PositionResponse")
    @Mapping(source = "Anchors", target = "AnchorsResponse")
    @Mapping(source = "MatchHistory", target = "MatchHistoryResponse")
    StateRegionResponse mapToResponse(StateRegion stateRegion);
    @Mapping(source = "RegionResponse", target = "Region")
    @Mapping(source = "PositionResponse", target = "Position")
    @Mapping(source = "AnchorsResponse", target = "Anchors")
    @Mapping(source = "MatchHistoryResponse", target = "MatchHistory")
    StateRegion mapFromResponse(StateRegionResponse stateRegionResponse);

}
