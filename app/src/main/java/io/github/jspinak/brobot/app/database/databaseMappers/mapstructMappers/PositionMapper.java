package io.github.jspinak.brobot.app.database.databaseMappers.mapstructMappers;

import io.github.jspinak.brobot.app.database.embeddable.PositionEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PositionMapper {
    PositionMapper INSTANCE = Mappers.getMapper(PositionMapper.class);

    PositionEmbeddable map(Position position);
    Position map(PositionEmbeddable positionEmbeddable);
}
