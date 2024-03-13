package com.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface MatchSnapshotResponseMapper {

    MatchSnapshotResponseMapper INSTANCE = Mappers.getMapper(MatchSnapshotResponseMapper.class);

    @Mapping(source = "ActionOptions", target = "ActionOptionsResponse")
    @Mapping(target = "matchList", source = "matchList", qualifiedByName = "mapMatchToMatchResponse")
    MatchSnapshotResponse mapToResponse(MatchSnapshot matchSnapshot);

    @Mapping(source = "ActionOptionsResponse", target = "ActionOptions")
    @Mapping(target = "matchList", source = "matchList", qualifiedByName = "mapMatchResponseToMatch")
    MatchSnapshot mapFromResponse(MatchSnapshotResponse matchSnapshotResponse);

}
