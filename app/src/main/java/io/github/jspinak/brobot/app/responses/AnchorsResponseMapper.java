package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = AnchorResponseMapper.class)
public interface AnchorsResponseMapper {
    AnchorsResponseMapper INSTANCE = Mappers.getMapper(AnchorsResponseMapper.class);

    @Mapping(source = "anchorList", target = "anchorList")
    AnchorsResponse map(Anchors anchors);

    @Mapping(source = "anchorList", target = "anchorList")
    Anchors map(AnchorsResponse anchorsResponse);
}