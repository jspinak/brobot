package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.embeddable.SearchRegionsEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {RegionMapper.class})
public interface SearchRegionsMapper {

    SearchRegionsMapper INSTANCE = Mappers.getMapper(SearchRegionsMapper.class);

    /*
    Maps a list of Region objects, where Region is embeddable.
    Maps a single Region called fixedRegion.
    SearchRegions is itself embeddable and not an entity.
     */
    @Mapping(target = "regions", source = "regions")
    @Mapping(source = "fixedRegion", target = "fixedRegion")
    SearchRegionsEmbeddable map(SearchRegions searchRegions);
    @Mapping(target = "regions", source = "regions")
    @Mapping(source = "fixedRegion", target = "fixedRegion")
    SearchRegions map(SearchRegionsEmbeddable searchRegionsEmbeddable);

}
