package io.github.jspinak.brobot.app.database.databaseMappers.mapstructMappers;

import io.github.jspinak.brobot.app.database.entities.StateRegionEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {RegionMapper.class, PositionMapper.class, AnchorsMapper.class,
        MatchHistoryMapper.class})
public interface StateRegionMapper {

    StateRegionMapper INSTANCE = Mappers.getMapper(StateRegionMapper.class);

    @Mapping(source = "searchRegion", target = "searchRegion")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "anchors", target = "anchors")
    @Mapping(source = "matchHistory", target = "matchHistory")
    @Mapping(target = "id", ignore = true)
    StateRegionEntity map(StateRegion stateRegion);
    @Mapping(source = "searchRegion", target = "searchRegion")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "anchors", target = "anchors")
    @Mapping(source = "matchHistory", target = "matchHistory")
    StateRegion map(StateRegionEntity stateRegionEntity);

}
