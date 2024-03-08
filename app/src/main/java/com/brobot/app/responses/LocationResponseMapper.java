package com.brobot.app.responses;

import com.brobot.app.database.entities.LocationEntity;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LocationResponseMapper {

    LocationResponseMapper INSTANCE = Mappers.getMapper(LocationResponseMapper.class);

    @Mapping(source = "Region", target = "RegionResponse")
    @Mapping(source = "Position", target = "PositionResponse")
    LocationResponse mapToResponse(Location location);

    @Mapping(source = "RegionResponse", target = "Region")
    @Mapping(source = "PositionResponse", target = "Position")
    Location mapFromResponse(LocationResponse locationResponse);
}
