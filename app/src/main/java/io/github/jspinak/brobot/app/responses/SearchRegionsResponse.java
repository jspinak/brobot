package io.github.jspinak.brobot.app.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SearchRegionsResponse {

    private List<RegionResponse> regions = new ArrayList<>();
    private RegionResponse fixedRegion = new RegionResponse();

}
