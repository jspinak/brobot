package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface StateLocationResponseMapper {

    StateLocationResponseMapper INSTANCE = Mappers.getMapper(StateLocationResponseMapper.class);

    @Mapping(source = "Location", target = "LocationResponse")
    @Mapping(source = "Position", target = "PositionResponse")
    @Mapping(source = "Anchors", target = "AnchorsResponse")
    @Mapping(source = "MatchHistory", target = "MatchHistoryResponse")
    StateLocationResponse mapToResponse(StateLocation stateLocation);
    @Mapping(source = "LocationResponse", target = "Location")
    @Mapping(source = "PositionResponse", target = "Position")
    @Mapping(source = "AnchorsResponse", target = "Anchors")
    @Mapping(source = "MatchHistoryResponse", target = "MatchHistory")
    StateLocation mapFromResponse(StateLocationResponse stateLocationResponse);

}
