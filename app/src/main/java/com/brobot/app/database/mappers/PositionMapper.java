package com.brobot.app.database.mappers;

import com.brobot.app.database.embeddable.PositionEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface PositionMapper {
    PositionMapper INSTANCE = Mappers.getMapper(PositionMapper.class);

    PositionEmbeddable mapToEmbeddable(Position position);
    Position mapFromEmbeddable(PositionEmbeddable positionEmbeddable);
}
