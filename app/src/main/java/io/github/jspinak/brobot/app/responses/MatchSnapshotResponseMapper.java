package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {ActionOptionsResponseMapper.class, MatchResponseMapper.class})
public interface MatchSnapshotResponseMapper {

    MatchSnapshotResponseMapper INSTANCE = Mappers.getMapper(MatchSnapshotResponseMapper.class);

    @Mapping(source = "actionOptions", target = "actionOptions")
    @Mapping(target = "matchList", source = "matchList")
    MatchSnapshotResponse map(MatchSnapshot matchSnapshot);

    @Mapping(source = "actionOptions", target = "actionOptions")
    @Mapping(target = "matchList", source = "matchList")
    MatchSnapshot map(MatchSnapshotResponse matchSnapshotResponse);

}
