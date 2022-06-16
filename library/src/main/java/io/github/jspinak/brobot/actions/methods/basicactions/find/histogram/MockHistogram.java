package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.mock.Mock;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class MockHistogram {

    private Mock mock;

    public MockHistogram(Mock mock) {
        this.mock = mock;
    }

    /*
    Converts mock matches into HistogramMatches by adding a random score.
    */
    public HistogramMatches getMockHistogramMatches(ActionOptions actionOptions, StateImageObject image,
                                                    List<Region> searchRegions) {
        HistogramMatches histMatches = new HistogramMatches();
        searchRegions.forEach(searchReg ->
                mock.getMatches(image, searchReg, actionOptions).getMatchRegions().forEach(
                        reg -> histMatches.addRegion(reg, (double)new Random().nextInt(100))));
        return histMatches;
    }
}
