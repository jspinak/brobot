package io.github.jspinak.brobot.log.model;

import io.github.jspinak.brobot.testingAUTs.ActionLog;
import org.mapstruct.factory.Mappers;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface ActionLogMapper {
    ActionLogMapper INSTANCE = Mappers.getMapper(ActionLogMapper.class);

    ActionLogDTO mapToDTO(ActionLog actionLog);

    ActionLog mapFromDTO(ActionLogDTO actionLogDTO);
}
