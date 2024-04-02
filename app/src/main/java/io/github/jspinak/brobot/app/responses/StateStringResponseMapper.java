package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {RegionResponseMapper.class})
public interface StateStringResponseMapper {

    StateStringResponseMapper INSTANCE = Mappers.getMapper(StateStringResponseMapper.class);

    @Mapping(source = "searchRegion", target = "searchRegion")
    StateStringResponse map(StateString stateString);
    @Mapping(source = "searchRegion", target = "searchRegion")
    StateString map(StateStringResponse stateStringResponse);

}
