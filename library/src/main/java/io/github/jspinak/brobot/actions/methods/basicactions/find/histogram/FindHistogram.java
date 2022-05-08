package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.SelectRegions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FindHistogram {

    private FindAllHistograms findAllHistograms;
    private SelectRegions selectRegions;
    private Time time;
    private MockHistogram mockHistogram;

    public FindHistogram(FindAllHistograms findAllHistograms, SelectRegions selectRegions,
                         Time time, MockHistogram mockHistogram) {
        this.findAllHistograms = findAllHistograms;
        this.selectRegions = selectRegions;
        this.time = time;
        this.mockHistogram = mockHistogram;
    }

    public Matches getMatches(ActionOptions actionOptions, List<StateImageObject> images) {
        Matches matches = new Matches();
        List<HistogramMatches> histMatches = new ArrayList<>();
        images.forEach(img -> histMatches.add(forOneImage(actionOptions, img)));
        double minCorr = getMinCorrelation(histMatches, actionOptions);
        int maxRegs = actionOptions.getMaxMatchesToActOn();
        histMatches.forEach(histM -> histM.getResults(minCorr).forEach((reg, corr) -> {
            if (maxRegs <= 0 || matches.size() < maxRegs) {
                try {
                    matches.add(new MatchObject(reg.toMatch(), histM.getStateImageObject(),
                            time.getDuration(actionOptions.getAction()).getSeconds()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
        return matches;
    }

    /*
    MinCorrelation is set to 0 if maxMatches is not active, or if the HistogramMatches contains less
    Regions than maxMatches. Otherwise, it returns the correlation in the maxMatches spot after
    the HistogramMatches have been sorted.
    */
    private double getMinCorrelation(List<HistogramMatches> histMatches, ActionOptions actionOptions) {
        if (actionOptions.getMaxMatchesToActOn() <= 0) return 0; // select all Regions
        HistogramMatches allMatches = new HistogramMatches();
        histMatches.forEach(histMatch -> allMatches.addRegions(histMatch.getRegionsScores()));
        return allMatches.getNthCorrelation(actionOptions.getMaxMatchesToActOn());
    }

    private HistogramMatches forOneImage(ActionOptions actionOptions, StateImageObject image) {
        List<Region> searchRegions = selectRegions.getRegions(actionOptions, image);
        if (BrobotSettings.mock)
            return mockHistogram.getMockHistogramMatches(actionOptions, image, searchRegions);
        HistogramMatches histMatches = new HistogramMatches();
        histMatches.setStateImageObject(image);
        searchRegions.forEach(reg -> histMatches.addRegions(findAllHistograms.find(reg, image.getImage())));
        histMatches.printFirst(50);
        return histMatches;
    }

}
