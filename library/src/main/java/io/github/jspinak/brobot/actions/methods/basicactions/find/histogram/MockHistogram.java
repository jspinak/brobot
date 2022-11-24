package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class MockHistogram {

    /**
     * Converts mock matches into HistogramMatches by adding a random score.
     * @param image the image with the histogram to find
     * @param searchRegions the regions in which to search
     * @return
     */
    public List<MatchObject> getMockHistogramMatches(StateImageObject image, List<Region> searchRegions) {
        List<MatchObject> matchObjects = new ArrayList<>();
        for (Region searchRegion : searchRegions) {
            try {
                matchObjects.add(new MatchObject(searchRegion.toMatch(), image, new Random().nextInt(100)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return matchObjects;
    }
}
