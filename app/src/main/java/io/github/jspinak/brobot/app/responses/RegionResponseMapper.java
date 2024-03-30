package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface RegionResponseMapper {
    RegionResponseMapper INSTANCE = Mappers.getMapper(RegionResponseMapper.class);
    RegionResponse mapToResponse(Region region);
    Region mapFromResponse(RegionResponse regionResponse);
}
