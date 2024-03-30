package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.entities.MatchesEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface MatchesMapper {

    MatchesMapper INSTANCE = Mappers.getMapper(MatchesMapper.class);

    @Mapping(source = "matchList", target = "matchList", qualifiedByName = "mapToMatchEntity")
    @Mapping(source = "initialMatchList", target = "initialMatchList", qualifiedByName = "mapToMatchEntity")
    @Mapping(source = "ActionOptions", target = "ActionOptionsEntity")
    @Mapping(source = "definedRegions", target = "definedRegions", qualifiedByName = "mapToRegionEmbeddable")
    MatchesEntity mapToEntity(Matches matches);
    @Mapping(source = "matchList", target = "matchList", qualifiedByName = "mapFromMatchEntity")
    @Mapping(source = "initialMatchList", target = "initialMatchList", qualifiedByName = "mapFromMatchEntity")
    @Mapping(source = "ActionOptions", target = "ActionOptionsEntity")
    @Mapping(source = "definedRegions", target = "definedRegions", qualifiedByName = "mapFromRegionEmbeddable")
    Matches mapFromEntity(MatchesEntity matchesEntity);

}
