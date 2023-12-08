package io.github.jspinak.brobot.actions.methods.basicactions.find.motion;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.MatchOps;
import io.github.jspinak.brobot.actions.methods.basicactions.find.SelectRegions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetSceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.contours.Contours;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Finds the regions with dynamic pixels. Typically, the SceneAnalysisCollection has many scenes and
 * an area that experiences movement in any scene is considered a region of motion.
 * ObjectCollection(s) are not used.
 */
@Component
public class FindRegionsOfMotion {

    private final GetSceneAnalysisCollection getSceneAnalysisCollection;
    private final SelectRegions selectRegions;
    private final FindDynamicPixels findDynamicPixels;
    private final IllustrateMotion illustrateMotion;
    private final MatchOps matchOps;

    public FindRegionsOfMotion(GetSceneAnalysisCollection getSceneAnalysisCollection,
                               SelectRegions selectRegions, FindDynamicPixels findDynamicPixels,
                               IllustrateMotion illustrateMotion, MatchOps matchOps) {
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
        this.selectRegions = selectRegions;
        this.findDynamicPixels = findDynamicPixels;
        this.illustrateMotion = illustrateMotion;
        this.matchOps = matchOps;
    }

    public Matches find(ActionOptions actionOptions, List<ObjectCollection> objectCollections) {
        Matches matches = new Matches();
        int scenes = actionOptions.getTimesToRepeatIndividualAction();
        double pause = actionOptions.getPauseBetweenIndividualActions();
        SceneAnalysisCollection sceneAnalysisCollection = getSceneAnalysisCollection.get(
                objectCollections, scenes, pause, actionOptions);
        if (sceneAnalysisCollection.getSceneAnalyses().size() < 2) {
            Report.println("Not enough scenes to detect motion");
            return matches;
        }
        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
        List<Region> searchRegions = selectRegions.getRegionsForAllImages(actionOptions, objectCollections.toArray(new ObjectCollection[0]));
        List<Match> dynamicPixelRegions = getDynamicRegions(sceneAnalysisCollection, actionOptions, searchRegions);
        matches.getSceneAnalysisCollection().getSceneAnalyses().forEach(sA -> {
            sA.setMatchList(dynamicPixelRegions);
            sA.getIllustrations().setMotion(sceneAnalysisCollection.getResults().clone());
            sA.getIllustrations().setMotionWithMatches(sA.getScene().getBgr());
        });
        matchOps.addGenericMatchObjects(dynamicPixelRegions, matches, actionOptions); // this is for the last scene
        matches.sortByMatchScoreDecending();
        matchOps.limitNumberOfMatches(matches, actionOptions);
        matches.setPixelMatches(sceneAnalysisCollection.getResults()); // pixelMatches = dynamic pixels
        return matches;
    }

    /**
     * Finds regions that change and saves the results in the SceneAnalysisCollection.
     * Writes illustrated scenes to the history folder.
     * @param sceneAnalysisCollection the scenes to analyze
     * @param actionOptions gives us the min and max areas for contours
     * @param searchRegions sets the region for acceptable contours
     * @return a list of Match objects representing areas of motion
     */
    public List<Match> getDynamicRegions(SceneAnalysisCollection sceneAnalysisCollection,
                                          ActionOptions actionOptions, List<Region> searchRegions) {
        List<Mat> scenes = sceneAnalysisCollection.getAllScenesAsBGR();
        MatVector scenesVector = new MatVector(scenes.toArray(new Mat[0]));
        //scenes.forEach(scenesVector::push_back);
        if (searchRegions.isEmpty()) searchRegions.add(new Region(0, 0, scenes.get(0).cols(), scenes.get(0).rows()));
        Mat dynamicPixels = findDynamicPixels.getDynamicPixelMask(scenesVector);
        sceneAnalysisCollection.setResults(dynamicPixels.clone());
        Contours contours = new Contours.Builder()
                .setBgrFromClassification2d(dynamicPixels)
                .setMinArea(actionOptions.getMinArea())
                .setMaxArea(actionOptions.getMaxArea())
                .setSearchRegions(searchRegions)
                .build();
        illustrateMotion.illustrateMotion(sceneAnalysisCollection, scenes.size()-1, dynamicPixels, contours);
        return contours.getMatches();
    }
}
