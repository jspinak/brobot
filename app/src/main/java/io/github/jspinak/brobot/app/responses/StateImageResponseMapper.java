package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {PatternResponseMapper.class})
public interface StateImageResponseMapper {

    StateImageResponseMapper INSTANCE = Mappers.getMapper(StateImageResponseMapper.class);

    @Mapping(target = "patterns", source = "patterns")
    StateImageResponse map(StateImage stateImage);

    @Mapping(target = "patterns", source = "patterns")
    StateImage map(StateImageResponse stateImageResponse);

}
