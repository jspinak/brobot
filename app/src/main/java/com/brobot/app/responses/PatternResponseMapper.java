package com.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface PatternResponseMapper {

    PatternResponseMapper INSTANCE = Mappers.getMapper(PatternResponseMapper.class);

    @Mapping(source = "SearchRegions", target = "SearchRegionsResponse")
    @Mapping(source = "MatchHistory", target = "MatchHistoryResponse")
    @Mapping(source = "Position", target = "PositionResponse")
    @Mapping(source = "Anchors", target = "AnchorsResponse")
    @Mapping(source = "Image", target = "ImageResponse")
    PatternResponse mapToResponse(Pattern pattern);
    @Mapping(source = "SearchRegionsResponse", target = "SearchRegions")
    @Mapping(source = "MatchHistoryResponse", target = "MatchHistory")
    @Mapping(source = "PositionResponse", target = "Position")
    @Mapping(source = "AnchorsResponse", target = "Anchors")
    @Mapping(source = "ImageResponse", target = "Image")
    Pattern mapFromResponse(PatternResponse patternResponse);

}
