package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {SearchRegionsResponseMapper.class, MatchHistoryResponseMapper.class,
        PositionResponseMapper.class, AnchorsResponseMapper.class, ImageResponseMapper.class})
public interface PatternResponseMapper {

    PatternResponseMapper INSTANCE = Mappers.getMapper(PatternResponseMapper.class);

    @Mapping(source = "searchRegions", target = "searchRegions")
    @Mapping(source = "matchHistory", target = "matchHistory")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "anchors", target = "anchors")
    @Mapping(source = "image", target = "image")
    PatternResponse map(Pattern pattern);
    @Mapping(source = "searchRegions", target = "searchRegions")
    @Mapping(source = "matchHistory", target = "matchHistory")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "anchors", target = "anchors")
    @Mapping(source = "image", target = "image")
    Pattern map(PatternResponse patternResponse);

}
