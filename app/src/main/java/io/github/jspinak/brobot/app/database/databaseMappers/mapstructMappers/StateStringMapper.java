package io.github.jspinak.brobot.app.database.databaseMappers.mapstructMappers;

import io.github.jspinak.brobot.app.database.entities.StateStringEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = RegionMapper.class)
public interface StateStringMapper {

    StateStringMapper INSTANCE = Mappers.getMapper(StateStringMapper.class);

    @Mapping(source = "searchRegion", target = "searchRegion")
    @Mapping(target = "id", ignore = true)
    StateStringEntity map(StateString stateString);
    @Mapping(source = "searchRegion", target = "searchRegion")
    StateString map(StateStringEntity stateStringEntity);

}
