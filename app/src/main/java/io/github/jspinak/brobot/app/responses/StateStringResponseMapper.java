package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface StateStringResponseMapper {

    StateStringResponseMapper INSTANCE = Mappers.getMapper(StateStringResponseMapper.class);

    @Mapping(source = "Region", target = "RegionResponse")
    StateStringResponse mapToResponse(StateString stateString);
    @Mapping(source = "RegionResponse", target = "Region")
    StateString mapFromResponse(StateStringResponse stateStringResponse);

}
