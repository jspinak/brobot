package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.entities.MatchesEntity;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {MatchMapper.class, ActionOptionsMapper.class, RegionMapper.class})
public interface MatchesMapper {

    MatchesMapper INSTANCE = Mappers.getMapper(MatchesMapper.class);

    @Mapping(source = "matchList", target = "matchList")
    @Mapping(source = "initialMatchList", target = "initialMatchList")
    @Mapping(source = "actionOptions", target = "actionOptions")
    @Mapping(source = "definedRegions", target = "definedRegions")
    @Mapping(target = "id", ignore = true)
    MatchesEntity map(Matches matches);
    @Mapping(source = "matchList", target = "matchList")
    @Mapping(source = "initialMatchList", target = "initialMatchList")
    @Mapping(source = "actionOptions", target = "actionOptions")
    @Mapping(source = "definedRegions", target = "definedRegions")
    Matches map(MatchesEntity matchesEntity);

}
