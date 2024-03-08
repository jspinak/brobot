package com.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StateStringResponseMapper {

    StateStringResponseMapper INSTANCE = Mappers.getMapper(StateStringResponseMapper.class);

    @Mapping(source = "Region", target = "RegionResponse")
    StateStringResponse mapToResponse(StateString stateString);
    @Mapping(source = "RegionResponse", target = "Region")
    StateString mapFromResponse(StateStringResponse stateStringResponse);

}
