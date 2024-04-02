package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {LocationResponseMapper.class, PositionResponseMapper.class,
        AnchorsResponseMapper.class, MatchHistoryResponseMapper.class})
public interface StateLocationResponseMapper {

    StateLocationResponseMapper INSTANCE = Mappers.getMapper(StateLocationResponseMapper.class);

    @Mapping(source = "location", target = "location")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "anchors", target = "anchors")
    @Mapping(source = "matchHistory", target = "matchHistory")
    StateLocationResponse map(StateLocation stateLocation);
    @Mapping(source = "location", target = "location")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "anchors", target = "anchors")
    @Mapping(source = "matchHistory", target = "matchHistory")
    StateLocation map(StateLocationResponse stateLocationResponse);

}
