package io.github.jspinak.brobot.datatypes.primitives.region;

import lombok.Getter;

@Getter
public class RegionResponse {

    private Long id = 0L;
    private int x = 0;
    private int y = 0;
    private int w = 0;
    private int h = 0;

    public RegionResponse(Region region) {
        if (region == null) return;
        id = region.getId();
        x = region.getX();
        y = region.getY();
        w = region.getW();
        h = region.getH();
    }
}
