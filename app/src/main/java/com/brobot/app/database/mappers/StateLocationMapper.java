package com.brobot.app.database.mappers;

import com.brobot.app.database.entities.StateLocationEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface StateLocationMapper {

    StateLocationMapper INSTANCE = Mappers.getMapper(StateLocationMapper.class);

    @Mapping(source = "Location", target = "LocationEntity")
    @Mapping(source = "Position", target = "PositionEmbeddable")
    @Mapping(source = "Anchors", target = "AnchorsEntity")
    @Mapping(source = "MatchHistory", target = "MatchHistoryEntity")
    StateLocationEntity mapToEntity(StateLocation stateLocation);
    @Mapping(source = "LocationEntity", target = "Location")
    @Mapping(source = "PositionEmbeddable", target = "Position")
    @Mapping(source = "AnchorsEntity", target = "Anchors")
    @Mapping(source = "MatchHistoryEntity", target = "MatchHistory")
    StateLocation mapFromEntity(StateLocationEntity stateLocationEntity);

}
