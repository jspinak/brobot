package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL) // Include non-null properties only
@Getter
@Setter
public class SearchRegionsResponse {

    private List<RegionResponse> regions = new ArrayList<>();
    private RegionResponse fixedRegion;

}