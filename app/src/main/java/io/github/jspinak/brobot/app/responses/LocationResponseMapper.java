package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {RegionResponseMapper.class, PositionResponseMapper.class})
public interface LocationResponseMapper {

    LocationResponseMapper INSTANCE = Mappers.getMapper(LocationResponseMapper.class);

    @Mapping(source = "region", target = "region")
    @Mapping(source = "position", target = "position")
    LocationResponse map(Location location);

    @Mapping(source = "region", target = "region")
    @Mapping(source = "position", target = "position")
    Location map(LocationResponse locationResponse);
}
