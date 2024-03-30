package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.entities.MatchSnapshotEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface MatchSnapshotMapper {

    MatchSnapshotMapper INSTANCE = Mappers.getMapper(MatchSnapshotMapper.class);

    @Mapping(source = "ActionOptions", target = "ActionOptionsEntity")
    @Mapping(target = "matchList", source = "matchList", qualifiedByName = "mapMatchToMatchEntity")
    MatchSnapshotEntity mapToEntity(MatchSnapshot matchSnapshot);

    @Mapping(source = "ActionOptionsEntity", target = "ActionOptions")
    @Mapping(target = "matchList", source = "matchList", qualifiedByName = "mapMatchEntityToMatch")
    MatchSnapshot mapFromEntity(MatchSnapshotEntity matchSnapshot);

}
