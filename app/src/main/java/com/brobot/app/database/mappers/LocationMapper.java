package com.brobot.app.database.mappers;

import com.brobot.app.database.entities.LocationEntity;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);

    @Mapping(source = "Region", target = "RegionEmbeddable")
    @Mapping(source = "Position", target = "PositionEmbeddable")
    @Mapping(source = "x", target = "locX")
    @Mapping(source = "y", target = "locY")
    LocationEntity mapToEntity(Location location);

    @Mapping(source = "RegionEmbeddable", target = "Region")
    @Mapping(source = "PositionEmbeddable", target = "Position")
    @Mapping(source = "locX", target = "x")
    @Mapping(source = "locY", target = "y")
    Location mapFromEntity(LocationEntity locationEntity);
}
