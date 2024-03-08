package io.github.jspinak.brobot.actions.methods.basicactions.find.motion;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.MatchOps;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.SelectRegions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetSceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.contours.Contours;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.MatVisualize;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
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
    private final MatchOps matchOps;
    private final MatVisualize matVisualize;

    public FindRegionsOfMotion(GetSceneAnalysisCollection getSceneAnalysisCollection,
                               SelectRegions selectRegions, FindDynamicPixels findDynamicPixels,
                               MatchOps matchOps, MatVisualize matVisualize) {
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
        this.selectRegions = selectRegions;
        this.findDynamicPixels = findDynamicPixels;
        this.matchOps = matchOps;
        this.matVisualize = matVisualize;
    }

    public void find(Matches matches, List<ObjectCollection> objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        int scenes = actionOptions.getTimesToRepeatIndividualAction();
        double pause = actionOptions.getPauseBetweenIndividualActions();
        SceneAnalysisCollection sceneAnalysisCollection = getSceneAnalysisCollection.get(
                objectCollections, scenes, pause, actionOptions);
        if (sceneAnalysisCollection.getSceneAnalyses().size() < 2) {
            Report.println("Not enough scenes to detect motion");
            return;
        }
        //System.out.println("FindRegionsOfMotion: # scenes = " + sceneAnalysisCollection.getSceneAnalyses().size());
        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
        List<Region> searchRegions = selectRegions.getRegionsForAllImages(actionOptions, objectCollections.toArray(new ObjectCollection[0]));
        List<Match> dynamicPixelRegions = getDynamicRegions(sceneAnalysisCollection, actionOptions, searchRegions);
        matches.getSceneAnalysisCollection().getSceneAnalyses().forEach(sA -> {
            sA.setMatchList(dynamicPixelRegions);
            sA.getIllustrations().setMotion(sceneAnalysisCollection.getResults());
            sA.getIllustrations().setMotionWithMatches(sA.getScene().getMatBGR());
        });
        matchOps.addMatchListToMatches(dynamicPixelRegions, matches); // this is for the last scene
        matchOps.limitNumberOfMatches(matches, actionOptions);
        matches.setMask(sceneAnalysisCollection.getResults()); // pixelMatches = dynamic pixels
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
        //System.out.println("FindRegionsOfMotion: beginning of getDynamicRegions");
        List<Mat> scenes = sceneAnalysisCollection.getAllScenesAsBGR();
        //System.out.println("FindRegionsOfMotion: number of scenes = " + scenes.size());
        MatVector scenesVector = new MatVector(scenes.toArray(new Mat[0]));
        if (searchRegions.isEmpty()) searchRegions.add(new Region(0, 0, scenes.get(0).cols(), scenes.get(0).rows()));
        Mat dynamicPixels = findDynamicPixels.getDynamicPixelMask(scenesVector);
        matVisualize.writeMatToHistory(dynamicPixels, "dynamicPixels");
        sceneAnalysisCollection.setResults(dynamicPixels); //.clone()
        //System.out.println("FindRegionsOfMotion: minArea = " + actionOptions.getMinArea() + " maxArea = " + actionOptions.getMaxArea() + " searchRegions: " + searchRegions);
        Contours contours = new Contours.Builder()
                .setBgrFromClassification2d(dynamicPixels)
                .setMinArea(actionOptions.getMinArea())
                .setMaxArea(actionOptions.getMaxArea())
                .setSearchRegions(searchRegions)
                .build();
        //System.out.println("FindRegionsOfMotion: # of contours = " + contours.getContours().size());
        sceneAnalysisCollection.setContours(contours);
        return contours.getMatchList();
    }
}
