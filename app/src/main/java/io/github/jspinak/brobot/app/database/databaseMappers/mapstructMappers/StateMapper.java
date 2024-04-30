package io.github.jspinak.brobot.app.database.databaseMappers.mapstructMappers;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.datatypes.state.state.State;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {StateLocationMapper.class, StateImageMapper.class,
StateRegionMapper.class, StateStringMapper.class, PatternMapper.class, ImageMapper.class, RegionMapper.class,
PositionMapper.class})
public interface StateMapper {

    StateMapper INSTANCE = Mappers.getMapper(StateMapper.class);

    @Mapping(target = "stateLocations", source = "stateLocations")
    @Mapping(target = "stateImages", source = "stateImages")
    @Mapping(target = "stateRegions", source = "stateRegions")
    @Mapping(target = "stateStrings", source = "stateStrings")
    //@Mapping(target = "matches", source = "matches")
    @Mapping(target = "scenes", source = "scenes")
    @Mapping(target = "illustrations", source = "illustrations")
    @Mapping(target = "id", ignore = true)
    StateEntity map(State state);

    @Mapping(target = "stateLocations", source = "stateLocations")
    @Mapping(target = "stateImages", source = "stateImages")
    @Mapping(target = "stateRegions", source = "stateRegions")
    @Mapping(target = "stateStrings", source = "stateStrings")
    //@Mapping(target = "matches", source = "matches")
    @Mapping(target = "scenes", source = "scenes")
    @Mapping(target = "illustrations", source = "illustrations")
    State map(StateEntity stateEntity);

}
