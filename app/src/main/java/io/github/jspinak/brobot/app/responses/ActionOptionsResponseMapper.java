package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {LocationResponseMapper.class, SearchRegionsResponseMapper.class})
public interface ActionOptionsResponseMapper {

    ActionOptionsResponseMapper INSTANCE = Mappers.getMapper(ActionOptionsResponseMapper.class);

    @Mapping(source = "locationAfterAction", target = "locationAfterAction")
    @Mapping(source = "offsetLocationBy", target = "offsetLocationBy")
    @Mapping(source = "searchRegions", target = "searchRegions")
    ActionOptionsResponse map(ActionOptions actionOptions);
    @Mapping(source = "locationAfterAction", target = "locationAfterAction")
    @Mapping(source = "offsetLocationBy", target = "offsetLocationBy")
    @Mapping(source = "searchRegions", target = "searchRegions")
    ActionOptions map(ActionOptionsResponse actionOptionsResponse);

}
