package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.entities.ObjectCollectionEntity;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface ObjectCollectionMapper {

    ObjectCollectionMapper INSTANCE = Mappers.getMapper(ObjectCollectionMapper.class);

    @Mapping(target = "stateLocations", source = "stateLocations", qualifiedByName = "mapStateLocationToStateLocationEntity")
    @Mapping(target = "stateImages", source = "stateImages", qualifiedByName = "mapStateImageToStateImageEntity")
    @Mapping(target = "stateRegions", source = "stateRegions", qualifiedByName = "mapStateRegionToStateRegionEntity")
    @Mapping(target = "stateStrings", source = "stateStrings", qualifiedByName = "mapStateStringToStateStringEntity")
    @Mapping(target = "matches", source = "matches", qualifiedByName = "mapMatchesToMatchesEntity")
    @Mapping(target = "scenes", source = "scenes", qualifiedByName = "mapPatternToPatternEntity")
    ObjectCollectionEntity mapToEntity(ObjectCollection objectCollection);

    @Mapping(target = "stateLocations", source = "stateLocations", qualifiedByName = "mapStateLocationEntityToStateLocation")
    @Mapping(target = "stateImages", source = "stateImages", qualifiedByName = "mapStateImageEntityToStateImage")
    @Mapping(target = "stateRegions", source = "stateRegions", qualifiedByName = "mapStateRegionEntityToStateRegion")
    @Mapping(target = "stateStrings", source = "stateStrings", qualifiedByName = "mapStateStringEntityToStateString")
    @Mapping(target = "matches", source = "matches", qualifiedByName = "mapMatchesEntityToMatches")
    @Mapping(target = "scenes", source = "scenes", qualifiedByName = "mapPatternEntityToPattern")
    ObjectCollection mapFromEntity(ObjectCollectionEntity objectCollectionEntity);

}
