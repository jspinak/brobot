package io.github.jspinak.brobot.app.database.databaseMappers.mapstructMappers;

import io.github.jspinak.brobot.app.database.embeddable.AnchorEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = PositionMapper.class)
public interface AnchorMapper {
    AnchorMapper INSTANCE = Mappers.getMapper(AnchorMapper.class);

    /*
     PositionMapper takes care of mapping for the embedded, non-Entity Position.
     MapToAnchor instead of MapToEntity when Anchor is not an @Entity
     */
    @Mapping(source = "positionInMatch", target = "positionInMatch")
    AnchorEmbeddable map(Anchor anchor);

    @Mapping(source = "positionInMatch", target = "positionInMatch")
    Anchor map(AnchorEmbeddable anchorEmbeddable);
}
