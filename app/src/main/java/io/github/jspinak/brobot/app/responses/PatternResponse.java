package io.github.jspinak.brobot.app.responses;

import lombok.Getter;

@Getter
public class PatternResponse {

    private String url = "";
    private String imgpath = "";
    private String name = "";
    private boolean fixed = true;
    private SearchRegionsResponse searchRegions = new SearchRegionsResponse();
    private boolean setKmeansColorProfiles = false;
    //private KmeansProfilesAllSchemas kmeansProfilesAllSchemas;
    //private ColorCluster colorCluster;
    private MatchHistoryResponse matchHistory = new MatchHistoryResponse();
    private int index = 0;
    private boolean dynamic = false;
    private PositionResponse position = new PositionResponse();
    private AnchorsResponse anchors = new AnchorsResponse();
    private ImageResponse image = new ImageResponse();
}
