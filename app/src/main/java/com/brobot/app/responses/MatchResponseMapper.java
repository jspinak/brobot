package com.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MatchResponseMapper {

    MatchResponseMapper INSTANCE = Mappers.getMapper(MatchResponseMapper.class);

    @Mapping(source = "Location", target = "LocationResponse")
    @Mapping(source = "Image", target = "ImageResponse")
    @Mapping(source = "Region", target = "RegionResponse")
    @Mapping(source = "Anchors", target = "AnchorsResponse")
    @Mapping(source = "stateObjectData", target = "stateObjectData", qualifiedByName = "mapToStateObjectData")
    MatchResponse mapToResponse(Match match);

    @Mapping(source = "LocationResponse", target = "Location")
    @Mapping(source = "ImageResponse", target = "Image")
    @Mapping(source = "RegionResponse", target = "Region")
    @Mapping(source = "AnchorsResponse", target = "Anchors")
    @Mapping(source = "stateObjectData", target = "stateObjectData", qualifiedByName = "mapFromStateObjectData")
    Match mapFromResponse(MatchResponse matchResponse);

}
