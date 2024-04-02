package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.database.entities.ActionOptionsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {LocationMapper.class, SearchRegionsMapper.class})
public interface ActionOptionsMapper {

    ActionOptionsMapper INSTANCE = Mappers.getMapper(ActionOptionsMapper.class);

    @Mapping(source = "locationAfterAction", target = "locationAfterAction")
    @Mapping(source = "offsetLocationBy", target = "offsetLocationBy")
    @Mapping(source = "searchRegions", target = "searchRegions")
    @Mapping(target = "id", ignore = true)
    ActionOptionsEntity map(ActionOptions actionOptions);
    @Mapping(source = "locationAfterAction", target = "locationAfterAction")
    @Mapping(source = "offsetLocationBy", target = "offsetLocationBy")
    @Mapping(source = "searchRegions", target = "searchRegions")
    ActionOptions map(ActionOptionsEntity actionOptionsEntity);

}
