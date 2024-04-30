package io.github.jspinak.brobot.app.database.databaseMappers.mapstructMappers;

import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StateObjectDataMapper {
    StateObjectDataMapper INSTANCE = Mappers.getMapper(StateObjectDataMapper.class);

    StateObjectData mapToStateObjectData(StateObjectData stateObjectData);
    StateObjectData mapFromStateObjectData(StateObjectData stateObjectData);
}
