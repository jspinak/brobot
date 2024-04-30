package io.github.jspinak.brobot.app.database.databaseMappers.mapstructMappers;

import io.github.jspinak.brobot.app.database.entities.MatchEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {LocationMapper.class, ImageMapper.class, RegionMapper.class,
        AnchorsMapper.class})
public interface MatchMapper {

    MatchMapper INSTANCE = Mappers.getMapper(MatchMapper.class);

    @Mapping(source = "target", target = "target")
    @Mapping(source = "image", target = "image")
    @Mapping(source = "region", target = "region")
    @Mapping(source = "anchors", target = "anchors")
    //@Mapping(source = "stateObjectData", target = "stateObjectData", qualifiedByName = "mapToStateObjectData")
    @Mapping(target = "id", ignore = true)
    MatchEntity map(Match match);

    @Mapping(source = "target", target = "target")
    @Mapping(source = "image", target = "image")
    @Mapping(source = "region", target = "region")
    @Mapping(source = "anchors", target = "anchors")
    //@Mapping(source = "stateObjectData", target = "stateObjectData", qualifiedByName = "mapFromStateObjectData")
    Match map(MatchEntity matchEntity);

}
