package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.state.state.State;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface StateResponseMapper {

    StateResponseMapper INSTANCE = Mappers.getMapper(StateResponseMapper.class);

    @Mapping(target = "stateLocations", source = "stateLocations", qualifiedByName = "mapStateLocationToStateLocationResponse")
    @Mapping(target = "stateImages", source = "stateImages", qualifiedByName = "mapStateImageToStateImageResponse")
    @Mapping(target = "stateRegions", source = "stateRegions", qualifiedByName = "mapStateRegionToStateRegionResponse")
    @Mapping(target = "stateStrings", source = "stateStrings", qualifiedByName = "mapStateStringToStateStringResponse")
    @Mapping(target = "matches", source = "matches", qualifiedByName = "mapMatchesToMatchesResponse")
    @Mapping(target = "scenes", source = "scenes", qualifiedByName = "mapImageToImageResponse")
    @Mapping(target = "illustrations", source = "illustrations", qualifiedByName = "mapImageToImageResponse")
    StateResponse mapToResponse(State state);

    @Mapping(target = "stateLocations", source = "stateLocations", qualifiedByName = "mapStateLocationResponseToStateLocation")
    @Mapping(target = "stateImages", source = "stateImages", qualifiedByName = "mapStateImageResponseToStateImage")
    @Mapping(target = "stateRegions", source = "stateRegions", qualifiedByName = "mapStateRegionResponseToStateRegion")
    @Mapping(target = "stateStrings", source = "stateStrings", qualifiedByName = "mapStateStringResponseToStateString")
    @Mapping(target = "matches", source = "matches", qualifiedByName = "mapMatchesResponseToMatches")
    @Mapping(target = "scenes", source = "scenes", qualifiedByName = "mapImageResponseToImage")
    @Mapping(target = "illustrations", source = "illustrations", qualifiedByName = "mapImageResponseToImage")
    State mapFromResponse(StateResponse stateResponse);

}
