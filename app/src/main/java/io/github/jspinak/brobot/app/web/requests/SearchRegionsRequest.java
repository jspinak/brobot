package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SearchRegionsRequest {
    private List<RegionRequest> regions = new ArrayList<>();
    private RegionRequest fixedRegion;
}
