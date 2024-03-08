package com.brobot.app.responses;

import com.brobot.app.database.mappers.AnchorMapper;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = AnchorMapper.class)
public interface AnchorsResponseMapper {
    AnchorsResponseMapper INSTANCE = Mappers.getMapper(AnchorsResponseMapper.class);

    @Mapping(source = "anchorList", target = "anchorList", qualifiedByName = "mapToAnchorResponse")
    AnchorsResponse mapToResponse(Anchors anchors);

    @Mapping(source = "anchorList", target = "anchorList", qualifiedByName = "mapFromAnchorResponse")
    Anchors mapFromResponse(AnchorsResponse anchorsResponse);
}