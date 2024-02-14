package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.datatypes.primitives.location.Anchors;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistory;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchHistoryResponse;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegions;
import io.github.jspinak.brobot.datatypes.primitives.region.SearchRegionsResponse;
import lombok.Getter;

@Getter
public class PatternResponse {

    private Long id = 0L;
    private String url = "";
    private String imgpath = "";
    private String name = "";
    private boolean fixed = true;
    private SearchRegionsResponse searchRegions = new SearchRegionsResponse(new SearchRegions());
    private boolean setKmeansColorProfiles = false;
    //private KmeansProfilesAllSchemas kmeansProfilesAllSchemas;
    //private ColorCluster colorCluster;
    private MatchHistoryResponse matchHistory = new MatchHistoryResponse(new MatchHistory());
    private int index = 0;
    private boolean dynamic = false;
    private Position position = new Position(.5, .5);
    private Anchors anchors = new Anchors();
    private ImageResponse image = new ImageResponse(null);

    public PatternResponse(Pattern pattern) {
        if (pattern == null) return;
        id = pattern.getId();
        url = pattern.getUrl();
        imgpath = pattern.getImgpath();
        name = pattern.getName();
        fixed = pattern.isFixed();
        searchRegions = new SearchRegionsResponse(pattern.getSearchRegions());
        setKmeansColorProfiles = pattern.isSetKmeansColorProfiles();
        matchHistory = new MatchHistoryResponse(pattern.getMatchHistory());
        index = pattern.getIndex();
        dynamic = pattern.isDynamic();
        position = pattern.getPosition();
        anchors = pattern.getAnchors();
        image = new ImageResponse(pattern.getImage());
    }

}
