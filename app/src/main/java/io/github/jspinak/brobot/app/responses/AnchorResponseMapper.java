package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = PositionResponseMapper.class)
public interface AnchorResponseMapper {
    AnchorResponseMapper INSTANCE = Mappers.getMapper(AnchorResponseMapper.class);

    @Mapping(source = "positionInMatch", target = "positionInMatch")
    AnchorResponse map(Anchor anchor);

    @Mapping(source = "positionInMatch", target = "positionInMatch")
    Anchor map(AnchorResponse anchorResponse);
}
