package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface StateImageResponseMapper {

    StateImageResponseMapper INSTANCE = Mappers.getMapper(StateImageResponseMapper.class);

    @Mapping(target = "patterns", source = "patterns", qualifiedByName = "mapPatternToPatternResponse")
    StateImageResponse mapToResponse(StateImage stateImage);

    @Mapping(target = "patterns", source = "patterns", qualifiedByName = "mapPatternResponseToPattern")
    StateImage mapFromResponse(StateImageResponse stateImageResponse);

}
