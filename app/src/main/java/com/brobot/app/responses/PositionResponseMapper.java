package com.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PositionResponseMapper {
    PositionResponseMapper INSTANCE = Mappers.getMapper(PositionResponseMapper.class);

    PositionResponse mapToResponse(Position position);
    Position mapFromResponse(PositionResponse positionResponse);
}
