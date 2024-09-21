package io.github.jspinak.brobot.app.web.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatternRequest {
    private Long id;
    private String url;
    private String imgpath;
    private String name;
    private boolean fixed;
    private SearchRegionsRequest searchRegions;
    private boolean setKmeansColorProfiles;
    private MatchHistoryRequest matchHistory;
    private int index;
    private boolean dynamic;
    private PositionRequest position;
    private AnchorsRequest anchors = new AnchorsRequest();
    private Long imageId;
}
