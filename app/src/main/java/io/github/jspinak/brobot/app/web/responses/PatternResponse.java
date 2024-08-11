package io.github.jspinak.brobot.app.web.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL) // Include non-null properties only
@Getter
@Setter
public class PatternResponse {

    private Long id;
    private String url;
    private String imgpath;
    private String name;
    private boolean fixed;
    private SearchRegionsResponse searchRegions;
    private boolean setKmeansColorProfiles;
    private MatchHistoryResponse matchHistory;
    private int index;
    private boolean dynamic;
    private PositionResponse position;
    private AnchorsResponse anchors = new AnchorsResponse();
    private ImageResponse image;

}