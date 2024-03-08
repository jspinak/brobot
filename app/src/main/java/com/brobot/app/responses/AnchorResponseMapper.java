package com.brobot.app.responses;

import com.brobot.app.database.mappers.PositionMapper;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = PositionMapper.class)
public interface AnchorResponseMapper {
    AnchorResponseMapper INSTANCE = Mappers.getMapper(AnchorResponseMapper.class);

    @Mapping(source = "Position", target = "PositionResponse")
    AnchorResponse mapToResponse(Anchor anchor);

    @Mapping(source = "PositionResponse", target = "Position")
    Anchor mapFromResponse(AnchorResponse anchorResponse);
}
