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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FindHistogram {

    private FindAllHistograms findAllHistograms;
    private SelectRegions selectRegions;
    private ImageRegionsHistograms imageRegionsHistograms;
    private Time time;
    private MockHistogram mockHistogram;

    public FindHistogram(FindAllHistograms findAllHistograms, SelectRegions selectRegions,
                         ImageRegionsHistograms imageRegionsHistograms, Time time,
                         MockHistogram mockHistogram) {
        this.findAllHistograms = findAllHistograms;
        this.selectRegions = selectRegions;
        this.imageRegionsHistograms = imageRegionsHistograms;
        this.time = time;
        this.mockHistogram = mockHistogram;
    }

    public Matches getMatches(ActionOptions actionOptions, List<StateImageObject> images) {
        imageRegionsHistograms.setBins(
                actionOptions.getHueBins(), actionOptions.getSaturationBins(), actionOptions.getValueBins());
        Matches matches = new Matches();
        List<HistogramMatches> histMatches = new ArrayList<>(); // holds the reg-scores for each Image
        images.forEach(img -> histMatches.add(forOneImage(actionOptions, img)));
        int maxRegs = actionOptions.getMaxMatchesToActOn();
        LinkedHashMap<MatchObject, Double> matchObjectsAndScore = new LinkedHashMap<>();
        // add maxRegs MatchObjects from each HistogramMatches object
        histMatches.forEach(histM -> histM.getFirstEntries(maxRegs).forEach((reg, score) -> {
            try {
                matchObjectsAndScore.put(
                        new MatchObject(reg.toMatch(), histM.getStateImageObject(),
                                time.getDuration(actionOptions.getAction()).getSeconds()),
                        score);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        // sort the MatchObjects and add the best ones (up to maxRegs) to matches
        matchObjectsAndScore.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(maxRegs)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
                .forEach(matches::add);
        return matches;
    }

    private HistogramMatches forOneImage(ActionOptions actionOptions, StateImageObject image) {
        List<Region> searchRegions = selectRegions.getRegions(actionOptions, image);
        //System.out.println("searchRegions: "+searchRegions);
        if (BrobotSettings.mock)
            return mockHistogram.getMockHistogramMatches(actionOptions, image, searchRegions);
        HistogramMatches histMatches = new HistogramMatches();
        histMatches.setStateImageObject(image);
        searchRegions.forEach(reg -> histMatches.addRegions(findAllHistograms.find(reg, image.getImage())));
        histMatches.sortLowToHigh();
        histMatches.printFirst(50);
        return histMatches;
    }

}
