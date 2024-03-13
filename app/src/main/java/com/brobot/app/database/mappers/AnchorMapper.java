package com.brobot.app.database.mappers;

import com.brobot.app.database.embeddable.AnchorEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", uses = PositionMapper.class)
@Component
public interface AnchorMapper {
    AnchorMapper INSTANCE = Mappers.getMapper(AnchorMapper.class);

    /*
     PositionMapper takes care of mapping for the embedded, non-Entity Position.
     MapToAnchor instead of MapToEntity when Anchor is not an @Entity
     */
    @Mapping(source = "Position", target = "PositionEmbeddable")
    AnchorEmbeddable mapToEmbeddable(Anchor anchor);

    @Mapping(source = "Position", target = "PositionEmbeddable")
    Anchor mapFromEmbeddable(AnchorEmbeddable anchorEmbeddable);
}
