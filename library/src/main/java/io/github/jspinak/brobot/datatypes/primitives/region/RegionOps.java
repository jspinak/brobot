package io.github.jspinak.brobot.datatypes.primitives.region;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

/**
 * Analysis functions involving Regions
 */
@Component
public class RegionOps {

    public boolean isWithinRegion(Location location, Region region) {
        return region.contains(location.getSikuliLocation());
    }

    public boolean matchesOverlap(Match match1, Match match2) {
        return new Region(match1).contains(new Region(match2));
    }

    public boolean objectsOverlap(Region region, Match match) {
        return region.contains(new Region(match));
    }
}
