package io.github.jspinak.brobot.app.database.databaseMappers.mapstructMappers;

import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {SearchRegionsMapper.class, MatchHistoryMapper.class,
        PositionMapper.class, AnchorsMapper.class, ImageMapper.class})
public interface PatternMapper {
    PatternMapper INSTANCE = Mappers.getMapper(PatternMapper.class);

    @Mapping(source = "searchRegions", target = "searchRegions")
    @Mapping(source = "matchHistory", target = "matchHistory")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "anchors", target = "anchors")
    @Mapping(source = "image", target = "image")
    @Mapping(target = "id", ignore = true)
    PatternEntity map(Pattern pattern);

    @Mapping(source = "searchRegions", target = "searchRegions")
    @Mapping(source = "matchHistory", target = "matchHistory")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "anchors", target = "anchors")
    @Mapping(source = "image", target = "image")
    Pattern map(PatternEntity patternEntity);
}
