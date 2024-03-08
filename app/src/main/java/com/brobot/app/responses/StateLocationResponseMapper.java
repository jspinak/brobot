package com.brobot.app.responses;

import com.brobot.app.database.entities.StateLocationEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
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
