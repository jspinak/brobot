package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.embeddable.SearchRegionsEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;

public class SearchRegionsEmbeddableMapper {

    public static SearchRegionsEmbeddable map(SearchRegions searchRegions) {
        SearchRegionsEmbeddable searchRegionsEmbeddable = new SearchRegionsEmbeddable();
        searchRegionsEmbeddable.setFixedRegion(RegionEmbeddableMapper.map(searchRegions.getFixedRegion()));
        searchRegionsEmbeddable.setRegions(RegionEmbeddableMapper.mapToRegionEmbeddableList(searchRegions.getRegions()));
        return searchRegionsEmbeddable;
    }

    public static SearchRegions map(SearchRegionsEmbeddable searchRegionsEmbeddable) {
        SearchRegions searchRegions = new SearchRegions();
        searchRegions.setFixedRegion(RegionEmbeddableMapper.map(searchRegionsEmbeddable.getFixedRegion()));
        searchRegions.setRegions(RegionEmbeddableMapper.mapToRegionList(searchRegionsEmbeddable.getRegions()));
        return searchRegions;
    }
}
