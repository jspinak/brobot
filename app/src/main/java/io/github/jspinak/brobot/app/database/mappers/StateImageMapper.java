package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {PatternMapper.class})
public interface StateImageMapper {
    StateImageMapper INSTANCE = Mappers.getMapper(StateImageMapper.class);

    @Mapping(target = "patterns", source = "patterns")
    @Mapping(target = "id", ignore = true)
    StateImageEntity map(StateImage stateImage);

    @Mapping(target = "patterns", source = "patterns")
    StateImage map(StateImageEntity stateImageEntity);
}
