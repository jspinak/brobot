package io.github.jspinak.brobot.tools.testing.mock.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Provides mock histogram matching functionality for testing and development. This class generates
 * simulated histogram matches without performing actual image analysis, useful for unit tests and
 * prototyping.
 *
 * <p>Mock matches are created with random similarity scores between 0 and 99, allowing tests to
 * verify histogram matching workflows without requiring actual image processing.
 *
 * @see Match
 * @see StateImage
 * @see Region
 */
@Component
public class MockHistogram {

    /**
     * Generates mock histogram matches for testing purposes. Creates one {@link Match} object for
     * each search region with a random similarity score between 0 and 99. This method simulates the
     * behavior of actual histogram matching without performing image analysis.
     *
     * @param image the {@link StateImage} to simulate finding (stored in match results)
     * @param searchRegions list of {@link Region} objects where matches should be simulated
     * @return list of {@link Match} objects with random similarity scores
     */
    public List<Match> getMockHistogramMatches(StateImage image, List<Region> searchRegions) {
        List<Match> matchObjects = new ArrayList<>();
        searchRegions.forEach(
                region ->
                        matchObjects.add(
                                new Match.Builder()
                                        .setRegion(region)
                                        .setStateObjectData(image)
                                        .setSimScore(new Random().nextInt(100))
                                        .build()));
        return matchObjects;
    }
}
