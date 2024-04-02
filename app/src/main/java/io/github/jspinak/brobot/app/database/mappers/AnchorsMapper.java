package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.entities.AnchorsEntity;
import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {AnchorMapper.class})
public interface AnchorsMapper {
    AnchorsMapper INSTANCE = Mappers.getMapper(AnchorsMapper.class);

    @Mapping(source = "anchorList", target = "anchorList")
    @Mapping(target = "id", ignore = true)
    AnchorsEntity map(Anchors anchors);

    @Mapping(source = "anchorList", target = "anchorList")
    Anchors map(AnchorsEntity anchorsEntity);
}