package com.brobot.app.database.mappers;

import com.brobot.app.database.entities.MatchSnapshotEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MatchSnapshotMapper {

    MatchSnapshotMapper INSTANCE = Mappers.getMapper(MatchSnapshotMapper.class);

    @Mapping(source = "ActionOptions", target = "ActionOptionsEntity")
    @Mapping(target = "matchList", source = "matchList", qualifiedByName = "mapMatchToMatchEntity")
    MatchSnapshotEntity mapToEntity(MatchSnapshot matchSnapshot);

    @Mapping(source = "ActionOptionsEntity", target = "ActionOptions")
    @Mapping(target = "matchList", source = "matchList", qualifiedByName = "mapMatchEntityToMatch")
    MatchSnapshot mapFromEntity(MatchSnapshotEntity matchSnapshot);

}
