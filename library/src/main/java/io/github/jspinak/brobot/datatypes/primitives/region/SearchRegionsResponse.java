package io.github.jspinak.brobot.datatypes.primitives.region;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SearchRegionsResponse {

    private Long id = 0L;
    private List<RegionResponse> regions = new ArrayList<>();
    private RegionResponse fixedRegion = new RegionResponse(new Region());

    public SearchRegionsResponse(SearchRegions searchRegions) {
        if (searchRegions == null) return;
        id = searchRegions.getId();
        regions = new ArrayList<>();
        searchRegions.getRegions().forEach(reg -> regions.add(new RegionResponse(reg)));
        fixedRegion = new RegionResponse(searchRegions.getFixedRegion());
    }

}
