package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetScenes;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.SelectRegions;
import io.github.jspinak.brobot.actions.methods.mockOrLiveInterface.MockOrLive;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class FindHistogram {
    private final SelectRegions selectRegions;
    private final GetHistograms getHistograms;
    private final GetScenes getScenes;
    private final MockOrLive mockOrLive;

    public FindHistogram(SelectRegions selectRegions, GetHistograms getHistograms, GetScenes getScenes,
                         MockOrLive mockOrLive) {
        this.selectRegions = selectRegions;
        this.getHistograms = getHistograms;
        this.getScenes = getScenes;
        this.mockOrLive = mockOrLive;
    }

    /**
     * The first ObjectCollection contains the images to find.
     * @param matches holds the action configuration and existing matches
     * @param objectCollections the images to search for
     */
    public void find(Matches matches, List<ObjectCollection> objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        if (actionOptions.getMaxMatchesToActOn() <= 0) actionOptions.setMaxMatchesToActOn(1); // default for histogram
        getHistograms.setBins(
                actionOptions.getHueBins(), actionOptions.getSaturationBins(), actionOptions.getValueBins());
        List<Image> scenes = getScenes.getScenes(actionOptions, objectCollections);
        SceneAnalysisCollection sceneAnalysisCollection = new SceneAnalysisCollection();
        List<Match> matchObjects = new ArrayList<>();
        objectCollections.get(0).getStateImages().forEach(img ->
                scenes.forEach(scene -> {
                    matchObjects.addAll(forOneImage(actionOptions, img, scene.getMatHSV()));
                    sceneAnalysisCollection.add(new SceneAnalysis(new ArrayList<>(), scene));
                }));
        int maxMatches = actionOptions.getMaxMatchesToActOn();
        // sort the MatchObjects and add the best ones (up to maxRegs) to matches
        matchObjects.stream()
                .sorted(Comparator.comparingDouble(Match::getScore))
                .limit(maxMatches)
                .forEach(matches::add);
        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
    }

    private List<Match> forOneImage(ActionOptions actionOptions, StateImage image, Mat sceneHSV) {
        List<Region> searchRegions = selectRegions.getRegions(actionOptions, image);
        List<Match> matchObjects = mockOrLive.findHistogram(image, sceneHSV, searchRegions);
        return matchObjects.stream().sorted(Comparator.comparing(Match::getScore)).toList();
    }

}
