package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {LocationResponseMapper.class, ImageResponseMapper.class,
        RegionResponseMapper.class, AnchorsResponseMapper.class})
public interface MatchResponseMapper {

    MatchResponseMapper INSTANCE = Mappers.getMapper(MatchResponseMapper.class);

    @Mapping(source = "target", target = "target")
    @Mapping(source = "image", target = "image")
    @Mapping(source = "region", target = "region")
    @Mapping(source = "anchors", target = "anchors")
    //@Mapping(source = "stateObjectData", target = "stateObjectData", qualifiedByName = "mapToStateObjectData")
    MatchResponse map(Match match);

    @Mapping(source = "target", target = "target")
    @Mapping(source = "image", target = "image")
    @Mapping(source = "region", target = "region")
    @Mapping(source = "anchors", target = "anchors")
    //@Mapping(source = "stateObjectData", target = "stateObjectData", qualifiedByName = "mapFromStateObjectData")
    Match map(MatchResponse matchResponse);

}
