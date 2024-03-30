package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.embeddable.RegionEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface RegionMapper {
    RegionMapper INSTANCE = Mappers.getMapper(RegionMapper.class);

    RegionEmbeddable mapToEmbeddable(Region region);
    Region mapFromEmbeddable(RegionEmbeddable regionEmbeddable);
}
