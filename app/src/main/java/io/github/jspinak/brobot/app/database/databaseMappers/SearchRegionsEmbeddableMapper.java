package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.embeddable.SearchRegionsEmbeddable;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import org.springframework.stereotype.Component;

@Component
public class SearchRegionsEmbeddableMapper {

    private final RegionEmbeddableMapper regionEmbeddableMapper;

    public SearchRegionsEmbeddableMapper(RegionEmbeddableMapper regionEmbeddableMapper) {
        this.regionEmbeddableMapper = regionEmbeddableMapper;
    }

    public SearchRegionsEmbeddable map(SearchRegions searchRegions) {
        SearchRegionsEmbeddable searchRegionsEmbeddable = new SearchRegionsEmbeddable();
        searchRegionsEmbeddable.setFixedRegion(regionEmbeddableMapper.map(searchRegions.getFixedRegion()));
        searchRegionsEmbeddable.setRegions(regionEmbeddableMapper.mapToRegionEmbeddableList(searchRegions.getRegions()));
        return searchRegionsEmbeddable;
    }

    public SearchRegions map(SearchRegionsEmbeddable searchRegionsEmbeddable) {
        SearchRegions searchRegions = new SearchRegions();
        searchRegions.setFixedRegion(regionEmbeddableMapper.map(searchRegionsEmbeddable.getFixedRegion()));
        searchRegions.setRegions(regionEmbeddableMapper.mapToRegionList(searchRegionsEmbeddable.getRegions()));
        return searchRegions;
    }
}
