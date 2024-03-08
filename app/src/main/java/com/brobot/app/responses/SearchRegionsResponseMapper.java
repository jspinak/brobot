package com.brobot.app.responses;

import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SearchRegionsResponseMapper {

    SearchRegionsResponseMapper INSTANCE = Mappers.getMapper(SearchRegionsResponseMapper.class);

    @Mapping(source = "regions", target = "regions", qualifiedByName = "mapToRegionResponse")
    @Mapping(source = "Region", target = "RegionResponse")
    SearchRegionsResponse mapToSearchRegions(SearchRegions searchRegions);
    @Mapping(source = "regions", target = "regions", qualifiedByName = "mapFromRegionResponse")
    @Mapping(source = "RegionResponse", target = "Region")
    SearchRegions mapFromSearchRegions(SearchRegionsResponse searchRegionsResponse);

}
