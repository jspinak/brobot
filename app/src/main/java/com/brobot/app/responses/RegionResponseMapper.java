package com.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RegionResponseMapper {
    RegionResponseMapper INSTANCE = Mappers.getMapper(RegionResponseMapper.class);
    RegionResponse mapToResponse(Region region);
    Region mapFromResponse(RegionResponse regionResponse);
}
