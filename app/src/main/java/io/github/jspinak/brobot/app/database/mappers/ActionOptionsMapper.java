package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.entities.ActionOptionsEntity;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface ActionOptionsMapper {

    ActionOptionsMapper INSTANCE = Mappers.getMapper(ActionOptionsMapper.class);

    @Mapping(source = "Location", target = "LocationEntity")
    @Mapping(source = "SearchRegions", target = "SearchRegionsEmbeddable")
    ActionOptionsEntity mapToEntity(ActionOptions actionOptions);
    @Mapping(source = "LocationEntity", target = "Location")
    @Mapping(source = "SearchRegionsEmbeddable", target = "SearchRegions")
    ActionOptions mapFromEntity(ActionOptionsEntity actionOptionsEntity);

}
