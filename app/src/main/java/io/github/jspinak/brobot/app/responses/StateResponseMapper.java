package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.state.state.State;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {StateLocationResponseMapper.class, StateImageResponseMapper.class,
        StateRegionResponseMapper.class, StateStringResponseMapper.class})
public interface StateResponseMapper {

    StateResponseMapper INSTANCE = Mappers.getMapper(StateResponseMapper.class);

    @Mapping(target = "stateLocations", source = "stateLocations")
    @Mapping(target = "stateImages", source = "stateImages")
    @Mapping(target = "stateRegions", source = "stateRegions")
    @Mapping(target = "stateStrings", source = "stateStrings")
    //@Mapping(target = "matches", source = "matches")
    @Mapping(target = "scenes", source = "scenes")
    @Mapping(target = "illustrations", source = "illustrations")
    StateResponse map(State state);

    @Mapping(target = "stateLocations", source = "stateLocations")
    @Mapping(target = "stateImages", source = "stateImages")
    @Mapping(target = "stateRegions", source = "stateRegions")
    @Mapping(target = "stateStrings", source = "stateStrings")
    //@Mapping(target = "matches", source = "matches")
    @Mapping(target = "scenes", source = "scenes")
    @Mapping(target = "illustrations", source = "illustrations")
    State map(StateResponse stateResponse);

}
