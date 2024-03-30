package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.app.database.mappers.AnchorMapper;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", uses = AnchorMapper.class)
@Component
public interface AnchorsResponseMapper {
    AnchorsResponseMapper INSTANCE = Mappers.getMapper(AnchorsResponseMapper.class);

    @Mapping(source = "anchorList", target = "anchorList", qualifiedByName = "mapToAnchorResponse")
    AnchorsResponse mapToResponse(Anchors anchors);

    @Mapping(source = "anchorList", target = "anchorList", qualifiedByName = "mapFromAnchorResponse")
    Anchors mapFromResponse(AnchorsResponse anchorsResponse);
}