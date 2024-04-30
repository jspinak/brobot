package io.github.jspinak.brobot.app.database.databaseMappers.mapstructMappers;

import io.github.jspinak.brobot.app.database.entities.MatchSnapshotEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {ActionOptionsMapper.class, MatchMapper.class})
public interface MatchSnapshotMapper {

    MatchSnapshotMapper INSTANCE = Mappers.getMapper(MatchSnapshotMapper.class);

    @Mapping(source = "actionOptions", target = "actionOptions")
    @Mapping(target = "matchList", source = "matchList")
    @Mapping(target = "id", ignore = true)
    MatchSnapshotEntity map(MatchSnapshot matchSnapshot);

    @Mapping(source = "actionOptions", target = "actionOptions")
    @Mapping(target = "matchList", source = "matchList")
    MatchSnapshot map(MatchSnapshotEntity matchSnapshot);

}
