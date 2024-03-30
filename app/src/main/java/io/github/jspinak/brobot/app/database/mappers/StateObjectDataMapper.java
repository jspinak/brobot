package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface StateObjectDataMapper {
    StateObjectDataMapper INSTANCE = Mappers.getMapper(StateObjectDataMapper.class);

    StateObjectData mapToStateObjectData(StateObjectData stateObjectData);
    StateObjectData mapFromStateObjectData(StateObjectData stateObjectData);
}
