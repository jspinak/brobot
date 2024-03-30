package io.github.jspinak.brobot.app.responses;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SearchRegionsResponse {

    private List<RegionResponse> regions = new ArrayList<>();
    private RegionResponse fixedRegion = new RegionResponse();

}
