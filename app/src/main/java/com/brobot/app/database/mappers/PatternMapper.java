package com.brobot.app.database.mappers;

import com.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
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
