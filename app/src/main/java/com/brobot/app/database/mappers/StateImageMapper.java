package com.brobot.app.database.mappers;

import com.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface StateImageMapper {

    StateImageMapper INSTANCE = Mappers.getMapper(StateImageMapper.class);

    @Mapping(target = "patterns", source = "patterns", qualifiedByName = "mapPatternToPatternEntity")
    StateImageEntity mapToEntity(StateImage stateImage);

    @Mapping(target = "patterns", source = "patterns", qualifiedByName = "mapPatternEntityToPattern")
    StateImage mapFromEntity(StateImageEntity stateImageEntity);

}
