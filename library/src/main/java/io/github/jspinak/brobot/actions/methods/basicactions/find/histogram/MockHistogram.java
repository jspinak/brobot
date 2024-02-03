package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
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
     * @return a list of Match objects
     */
    public List<Match> getMockHistogramMatches(StateImage image, List<Region> searchRegions) {
        List<Match> matchObjects = new ArrayList<>();
        searchRegions.forEach(region -> matchObjects.add(
                new Match.Builder()
                        .setRegion(region)
                        .setStateObjectData(image)
                        .setSimScore(new Random().nextInt(100))
                        .build()
        ));
        return matchObjects;
    }
}
