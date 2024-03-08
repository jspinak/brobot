package com.brobot.app.responses;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ActionOptionsResponseMapper {

    ActionOptionsResponseMapper INSTANCE = Mappers.getMapper(ActionOptionsResponseMapper.class);

    @Mapping(source = "Location", target = "LocationResponse")
    @Mapping(source = "SearchRegions", target = "SearchRegionsResponse")
    ActionOptionsResponse mapToResponse(ActionOptions actionOptions);
    @Mapping(source = "LocationResponse", target = "Location")
    @Mapping(source = "SearchRegionsResponse", target = "SearchRegions")
    ActionOptions mapFromResponse(ActionOptionsResponse actionOptionsResponse);

}
