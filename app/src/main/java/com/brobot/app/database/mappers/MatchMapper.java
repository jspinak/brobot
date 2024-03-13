package com.brobot.app.database.mappers;

import com.brobot.app.database.entities.MatchEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface MatchMapper {

    MatchMapper INSTANCE = Mappers.getMapper(MatchMapper.class);

    @Mapping(source = "Location", target = "LocationEntity")
    @Mapping(source = "Image", target = "ImageEntity")
    @Mapping(source = "Region", target = "RegionEmbeddable")
    @Mapping(source = "Anchors", target = "AnchorsEntity")
    @Mapping(source = "stateObjectData", target = "stateObjectData", qualifiedByName = "mapToStateObjectData")
    MatchEntity mapToEntity(Match match);

    @Mapping(source = "LocationEntity", target = "Location")
    @Mapping(source = "ImageEntity", target = "Image")
    @Mapping(source = "RegionEmbeddable", target = "Region")
    @Mapping(source = "AnchorsEntity", target = "Anchors")
    @Mapping(source = "stateObjectData", target = "stateObjectData", qualifiedByName = "mapFromStateObjectData")
    Match mapFromEntity(MatchEntity matchEntity);

}
