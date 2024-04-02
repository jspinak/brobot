package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.entities.StateLocationEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {LocationMapper.class, PositionMapper.class, AnchorsMapper.class,
        MatchHistoryMapper.class})
public interface StateLocationMapper {

    StateLocationMapper INSTANCE = Mappers.getMapper(StateLocationMapper.class);

    @Mapping(source = "location", target = "location")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "anchors", target = "anchors")
    @Mapping(source = "matchHistory", target = "matchHistory")
    @Mapping(target = "id", ignore = true)
    StateLocationEntity map(StateLocation stateLocation);
    @Mapping(source = "location", target = "location")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "anchors", target = "anchors")
    @Mapping(source = "matchHistory", target = "matchHistory")
    StateLocation map(StateLocationEntity stateLocationEntity);

}
