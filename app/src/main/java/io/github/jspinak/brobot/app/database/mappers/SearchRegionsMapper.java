package io.github.jspinak.brobot.app.database.mappers;

import io.github.jspinak.brobot.app.database.embeddable.SearchRegionsEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface SearchRegionsMapper {

    SearchRegionsMapper INSTANCE = Mappers.getMapper(SearchRegionsMapper.class);

    /*
    Maps a list of Region objects, where Region is embeddable.
    Maps a single Region called fixedRegion.
    SearchRegions is itself embeddable and not an entity.
     */
    @Mapping(target = "regions", source = "regions", qualifiedByName = "mapRegionToRegionEmbeddable")
    @Mapping(source = "Region", target = "RegionEmbeddable")
    SearchRegionsEmbeddable mapToEmbeddable(SearchRegions searchRegions);
    @Mapping(target = "regions", source = "regions", qualifiedByName = "mapRegionEmbeddableToRegion")
    @Mapping(source = "RegionEmbeddable", target = "Region")
    SearchRegions mapFromEmbeddable(SearchRegionsEmbeddable searchRegionsEmbeddable);

}
