package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.app.database.mappers.PositionMapper;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", uses = PositionMapper.class)
@Component
public interface AnchorResponseMapper {
    AnchorResponseMapper INSTANCE = Mappers.getMapper(AnchorResponseMapper.class);

    @Mapping(source = "Position", target = "PositionResponse")
    AnchorResponse mapToResponse(Anchor anchor);

    @Mapping(source = "PositionResponse", target = "Position")
    Anchor mapFromResponse(AnchorResponse anchorResponse);
}
