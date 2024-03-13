package com.brobot.app.database.mappers;

import com.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface StateMapper {

    StateMapper INSTANCE = Mappers.getMapper(StateMapper.class);

    @Mapping(target = "stateLocations", source = "stateLocations", qualifiedByName = "mapStateLocationToStateLocationEntity")
    @Mapping(target = "stateImages", source = "stateImages", qualifiedByName = "mapStateImageToStateImageEntity")
    @Mapping(target = "stateRegions", source = "stateRegions", qualifiedByName = "mapStateRegionToStateRegionEntity")
    @Mapping(target = "stateStrings", source = "stateStrings", qualifiedByName = "mapStateStringToStateStringEntity")
    @Mapping(target = "matches", source = "matches", qualifiedByName = "mapMatchesToMatchesEntity")
    @Mapping(target = "scenes", source = "scenes", qualifiedByName = "mapImageToImageEntity")
    @Mapping(target = "illustrations", source = "illustrations", qualifiedByName = "mapImageToImageEntity")
    StateEntity mapToEntity(State state);

    @Mapping(target = "stateLocations", source = "stateLocations", qualifiedByName = "mapStateLocationEntityToStateLocation")
    @Mapping(target = "stateImages", source = "stateImages", qualifiedByName = "mapStateImageEntityToStateImage")
    @Mapping(target = "stateRegions", source = "stateRegions", qualifiedByName = "mapStateRegionEntityToStateRegion")
    @Mapping(target = "stateStrings", source = "stateStrings", qualifiedByName = "mapStateStringEntityToStateString")
    @Mapping(target = "matches", source = "matches", qualifiedByName = "mapMatchesEntityToMatches")
    @Mapping(target = "scenes", source = "scenes", qualifiedByName = "mapImageEntityToImage")
    @Mapping(target = "illustrations", source = "illustrations", qualifiedByName = "mapImageEntityToImage")
    State mapFromEntity(StateEntity stateEntity);

}
