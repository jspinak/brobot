package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface PatternMapper {

    PatternMapper INSTANCE = Mappers.getMapper(PatternMapper.class);

    @Mapping(source = "searchRegions", target = "searchRegions", qualifiedByName = "mapToSearchRegions")
    @Mapping(source = "MatchHistory", target = "MatchHistoryEntity")
    @Mapping(source = "Position", target = "PositionEmbeddable")
    @Mapping(source = "Anchors", target = "AnchorsEntity")
    @Mapping(source = "Image", target = "ImageEntity")
    PatternEntity mapToEntity(Pattern pattern);
    @Mapping(source = "searchRegions", target = "searchRegions", qualifiedByName = "mapFromSearchRegions")
    @Mapping(source = "MatchHistoryEntity", target = "MatchHistory")
    @Mapping(source = "PositionEmbeddable", target = "Position")
    @Mapping(source = "AnchorsEntity", target = "Anchors")
    @Mapping(source = "ImageEntity", target = "Image")
    Pattern mapFromEntity(PatternEntity patternEntity);

}
