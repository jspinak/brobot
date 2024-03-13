package com.brobot.app.database.mappers;

import com.brobot.app.database.entities.StateRegionEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface StateRegionMapper {

    StateRegionMapper INSTANCE = Mappers.getMapper(StateRegionMapper.class);

    @Mapping(source = "Region", target = "RegionEmbeddable")
    @Mapping(source = "Position", target = "PositionEmbeddable")
    @Mapping(source = "Anchors", target = "AnchorsEntity")
    @Mapping(source = "MatchHistory", target = "MatchHistoryEntity")
    StateRegionEntity mapToEntity(StateRegion stateRegion);
    @Mapping(source = "RegionEmbeddable", target = "Region")
    @Mapping(source = "PositionEmbeddable", target = "Position")
    @Mapping(source = "AnchorsEntity", target = "Anchors")
    @Mapping(source = "MatchHistoryEntity", target = "MatchHistory")
    StateRegion mapFromEntity(StateRegionEntity stateRegionEntity);

}
