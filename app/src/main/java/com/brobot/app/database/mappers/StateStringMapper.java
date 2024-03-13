package com.brobot.app.database.mappers;

import com.brobot.app.database.entities.StateStringEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface StateStringMapper {

    StateStringMapper INSTANCE = Mappers.getMapper(StateStringMapper.class);

    @Mapping(source = "Region", target = "RegionEmbeddable")
    StateStringEntity mapToEntity(StateString stateString);
    @Mapping(source = "RegionEmbeddable", target = "Region")
    StateString mapFromEntity(StateStringEntity stateStringEntity);

}
