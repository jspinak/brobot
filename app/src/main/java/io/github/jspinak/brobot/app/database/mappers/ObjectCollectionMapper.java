package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.entities.ObjectCollectionEntity;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {StateLocationMapper.class, StateImageMapper.class, StateRegionMapper.class,
        StateStringMapper.class, MatchesMapper.class, PatternMapper.class})
public interface ObjectCollectionMapper {

    ObjectCollectionMapper INSTANCE = Mappers.getMapper(ObjectCollectionMapper.class);

    @Mapping(target = "stateLocations", source = "stateLocations")
    @Mapping(target = "stateImages", source = "stateImages")
    @Mapping(target = "stateRegions", source = "stateRegions")
    @Mapping(target = "stateStrings", source = "stateStrings")
    @Mapping(target = "matches", source = "matches")
    @Mapping(target = "scenes", source = "scenes")
    @Mapping(target = "id", ignore = true)
    ObjectCollectionEntity map(ObjectCollection objectCollection);

    @Mapping(target = "stateLocations", source = "stateLocations")
    @Mapping(target = "stateImages", source = "stateImages")
    @Mapping(target = "stateRegions", source = "stateRegions")
    @Mapping(target = "stateStrings", source = "stateStrings")
    @Mapping(target = "matches", source = "matches")
    @Mapping(target = "scenes", source = "scenes")
    ObjectCollection map(ObjectCollectionEntity objectCollectionEntity);

}
