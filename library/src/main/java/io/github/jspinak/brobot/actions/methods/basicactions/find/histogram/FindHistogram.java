package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.SelectRegions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetScenes;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
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

    private FindHistogramsOneRegionOneImage findHistogramsOneRegionOneImage;
    private SelectRegions selectRegions;
    private GetHistograms getHistograms;
    private MockHistogram mockHistogram;
    private GetScenes getScenes;
    private ActionLifecycleManagement actionLifecycleManagement;

    public FindHistogram(FindHistogramsOneRegionOneImage findHistogramsOneRegionOneImage, SelectRegions selectRegions,
                         GetHistograms getHistograms,
                         MockHistogram mockHistogram, GetScenes getScenes,
                         ActionLifecycleManagement actionLifecycleManagement) {
        this.findHistogramsOneRegionOneImage = findHistogramsOneRegionOneImage;
        this.selectRegions = selectRegions;
        this.getHistograms = getHistograms;
        this.mockHistogram = mockHistogram;
        this.getScenes = getScenes;
        this.actionLifecycleManagement = actionLifecycleManagement;
    }

    /**
     * The first ObjectCollection contains the images to find.
     * @param actionOptions the action configuration
     * @param objectCollections the images to search for
     * @return histogram matches
     */
    public Matches find(Matches matches, ActionOptions actionOptions, List<ObjectCollection> objectCollections) {
        if (actionOptions.getMaxMatchesToActOn() <= 0) actionOptions.setMaxMatchesToActOn(1); // default for histogram
        int actionId = actionLifecycleManagement.newActionLifecycle(actionOptions, matches);
        getHistograms.setBins(
                actionOptions.getHueBins(), actionOptions.getSaturationBins(), actionOptions.getValueBins());
        List<Scene> scenes = getScenes.getScenes(actionOptions, objectCollections);
        SceneAnalysisCollection sceneAnalysisCollection = new SceneAnalysisCollection();
        List<MatchObject> matchObjects = new ArrayList<>();
        objectCollections.get(0).getStateImages().forEach(img ->
                scenes.forEach(scene -> {
                    matchObjects.addAll(forOneImage(actionOptions, img, scene.getHsv(), actionId));
                    sceneAnalysisCollection.add(new SceneAnalysis(new ArrayList<>(), scene));
                }));
        int maxMatches = actionOptions.getMaxMatchesToActOn();
        // sort the MatchObjects and add the best ones (up to maxRegs) to matches
        matchObjects.stream()
                .sorted(Comparator.comparingDouble(MatchObject::getScore))
                .limit(maxMatches)
                .forEach(matches::add);
        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
        return matches;
    }

    private List<MatchObject> forOneImage(ActionOptions actionOptions, StateImage image, Mat sceneHSV, int actionId) {
        List<Region> searchRegions = selectRegions.getRegions(actionOptions, image);
        if (BrobotSettings.mock && BrobotSettings.screenshots.isEmpty())
            return mockHistogram.getMockHistogramMatches(image, searchRegions);
        List<MatchObject> matchObjects = new ArrayList<>();
        for (Region reg : searchRegions) {
            List<MatchObject> matchObjectsForOneRegion = findHistogramsOneRegionOneImage.find(reg, image, sceneHSV, actionId);
            matchObjects.addAll(matchObjectsForOneRegion);
        }
        matchObjects = matchObjects.stream().sorted(Comparator.comparing(MatchObject::getScore)).toList();
        return matchObjects;
    }

}
