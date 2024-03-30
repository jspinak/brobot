package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.entities.AnchorsEntity;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", uses = AnchorMapper.class)
@Component
public interface AnchorsMapper {
    AnchorsMapper INSTANCE = Mappers.getMapper(AnchorsMapper.class);

    @Mapping(source = "anchorList", target = "anchorList", qualifiedByName = "mapToAnchorEmbeddable")
    AnchorsEntity mapToEntity(Anchors anchors);

    @Mapping(source = "anchorList", target = "anchorList", qualifiedByName = "mapFromAnchorEmbeddable")
    Anchors mapFromEntity(AnchorsEntity anchorsEntity);
}