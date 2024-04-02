package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RegionResponseMapper {
    RegionResponseMapper INSTANCE = Mappers.getMapper(RegionResponseMapper.class);

    @Mapping(source = "x", target = "x")
    @Mapping(source = "y", target = "y")
    @Mapping(source = "w", target = "w")
    @Mapping(source = "h", target = "h")
    RegionResponse map(Region region);

    @Mapping(source = "x", target = "x")
    @Mapping(source = "y", target = "y")
    @Mapping(source = "w", target = "w")
    @Mapping(source = "h", target = "h")
    Region map(RegionResponse regionResponse);
}
