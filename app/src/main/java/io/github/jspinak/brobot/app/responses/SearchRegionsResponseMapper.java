package io.github.jspinak.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {RegionResponseMapper.class})
public interface SearchRegionsResponseMapper {

    SearchRegionsResponseMapper INSTANCE = Mappers.getMapper(SearchRegionsResponseMapper.class);

    @Mapping(source = "regions", target = "regions")
    @Mapping(source = "fixedRegion", target = "fixedRegion")
    SearchRegionsResponse mapToSearchRegions(SearchRegions searchRegions);
    @Mapping(source = "regions", target = "regions")
    @Mapping(source = "fixedRegion", target = "fixedRegion")
    SearchRegions mapFromSearchRegions(SearchRegionsResponse searchRegionsResponse);

}
