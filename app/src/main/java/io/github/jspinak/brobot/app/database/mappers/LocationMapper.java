package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.entities.LocationEntity;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {RegionMapper.class, PositionMapper.class})
public interface LocationMapper {

    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);

    @Mapping(source = "region", target = "region")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "x", target = "locX")
    @Mapping(source = "y", target = "locY")
    @Mapping(target = "id", ignore = true)
    LocationEntity map(Location location);

    @Mapping(source = "region", target = "region")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "locX", target = "x")
    @Mapping(source = "locY", target = "y")
    Location map(LocationEntity locationEntity);
}
