package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {RegionResponseMapper.class, PositionResponseMapper.class, AnchorsResponseMapper.class,
        MatchHistoryResponseMapper.class})
public interface StateRegionResponseMapper {

    StateRegionResponseMapper INSTANCE = Mappers.getMapper(StateRegionResponseMapper.class);

    @Mapping(source = "searchRegion", target = "searchRegion")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "anchors", target = "anchors")
    @Mapping(source = "matchHistory", target = "matchHistory")
    StateRegionResponse map(StateRegion stateRegion);
    @Mapping(source = "searchRegion", target = "searchRegion")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "anchors", target = "anchors")
    @Mapping(source = "matchHistory", target = "matchHistory")
    StateRegion map(StateRegionResponse stateRegionResponse);

}
