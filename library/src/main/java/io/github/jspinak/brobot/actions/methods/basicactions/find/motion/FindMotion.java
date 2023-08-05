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
import io.github.jspinak.brobot.illustratedHistory.Illustrations;
import io.github.jspinak.brobot.illustratedHistory.draw.DrawMatch;
import io.github.jspinak.brobot.imageUtils.MatVisualize;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis.Analysis.SCENE;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;

/**
 * TimeBetweenIndividualActions is the option in ActionOptions that gives the time (in seconds)
 * between the first, second, and third screenshots. These 3 screenshots will determine where motion
 * has occurred.
 */
@Component
public class FindMotion {

    private DetectMotion detectMotion;
    private MatchOps matchOps;
    private GetSceneAnalysisCollection getSceneAnalysisCollection;
    private SelectMovingObject selectMovingObject;
    private MatVisualize matVisulize;
    private SelectRegions selectRegions;
    private DrawMatch drawMatch;

    public FindMotion(DetectMotion detectMotion, MatchOps matchOps,
                      GetSceneAnalysisCollection getSceneAnalysisCollection, SelectMovingObject selectMovingObject,
                      MatVisualize matVisulize, SelectRegions selectRegions, DrawMatch drawMatch) {
        this.detectMotion = detectMotion;
        this.matchOps = matchOps;
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
        this.selectMovingObject = selectMovingObject;
        this.matVisulize = matVisulize;
        this.selectRegions = selectRegions;
        this.drawMatch = drawMatch;
    }

    /**
     * When images are passed, motion between the images will be returned. Images must be of the same size.
     * To find motion on the screen, call Find.MOTION without an ObjectCollection or with an empty ObjectCollection.
     * A minimum of 3 scenes are needed to show the object that is moving. WIth 2 scenes, movement can be
     * detected, but it is unclear what is the object and what is the background. Capturing movement entails
     * finding changed pixels from scenes 1 and 2 in a different place in scene 3. Changed pixels from scenes 1 and 2
     * that are in the same place as in scene 1 or 2 are considered part of the background, and are not moving objects.
     * The background is assumed to be the same in all 3 scenes. The tricky part is identifying the moving object,
     * since the moving object may change it's color, shape, or size. A moving object can be identified by having a
     * similar trajectory to an object in scenes 1 and 2, and this is why 3 scenes are needed.
     *
     * if images are provided, they can be used to select specific objects that have moved. If no images are provided,
     * all moving objects will be returned.
     *
     * @param actionOptions The action's configuration
     * @param objectCollections holds the images to analyze for motion in the first collection
     * @return a Matches object with matches for objects that have moved.
     */
    public Matches find(ActionOptions actionOptions, List<ObjectCollection> objectCollections) {
        Matches matches = new Matches();
        SceneAnalysisCollection sceneAnalysisCollection = getSceneAnalysisCollection.get(
                objectCollections, 3, 0.1, actionOptions);
        if (sceneAnalysisCollection.getSceneAnalyses().size() < 3) {
            Report.println("Not enough scenes to detect motion");
            return matches;
        }
        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
        List<Region> searchRegions = selectRegions.getRegionsForAllImages(actionOptions, objectCollections.toArray(new ObjectCollection[0]));
        List<Match> matchList1 = getRegionsOfChange(sceneAnalysisCollection, 0, 1, actionOptions, searchRegions);
        List<Match> matchList2 = getRegionsOfChange(sceneAnalysisCollection, 1, 2, actionOptions, searchRegions);
        List<List<Match>> movingObjects = selectMovingObject.select(matchList1, matchList2, actionOptions.getMaxMovement());
        matches.getSceneAnalysisCollection().getSceneAnalyses().get(0).setMatchList(movingObjects.get(0));
        matches.getSceneAnalysisCollection().getSceneAnalyses().get(1).setMatchList(movingObjects.get(1));
        matches.getSceneAnalysisCollection().getSceneAnalyses().get(2).setMatchList(movingObjects.get(2));
        sceneAnalysisCollection.getSceneAnalyses().forEach(sA -> {
            sA.getIllustrations().setMotion(sA.getScene().getBgr());
            sA.getIllustrations().setMotionWithMatches(sA.getScene().getBgr());
        });
        matchOps.addGenericMatchObjects(movingObjects.get(2), matches, actionOptions); // this is for the last scene
        matches.sortByMatchScoreDecending();
        matchOps.limitNumberOfMatches(matches, actionOptions);
        return matches;
    }

    private List<Match> getRegionsOfChange(SceneAnalysisCollection sceneAnalysisCollection, int index1, int index2,
                                           ActionOptions actionOptions, List<Region> searchRegions) {
        Mat scene1 = sceneAnalysisCollection.getSceneAnalyses().get(index1).getAnalysis(BGR, SCENE);
        Mat scene2 = sceneAnalysisCollection.getSceneAnalyses().get(index2).getAnalysis(BGR, SCENE);
        if (searchRegions.isEmpty()) searchRegions.add(new Region(0, 0, scene1.cols(), scene1.rows()));
        Mat absdiff = detectMotion.getAbsdiff(scene1, scene2);
        Contours contours = new Contours.Builder()
                .setBgrFromClassification2d(absdiff) // absdiff works just as well for contours as the BGR_CLASSIFICATION_2D Mat would
                .setMinArea(actionOptions.getMinArea())
                .setMaxArea(actionOptions.getMaxArea())
                .setSearchRegions(searchRegions)
                .build();
        Illustrations illustrations = sceneAnalysisCollection.getSceneAnalyses().get(index2).getIllustrations();
        illustrations.setMotion(absdiff);
        Mat motionWithMatches = absdiff.clone();
        drawMatch.drawMatches(motionWithMatches, contours.getMatches(), new Scalar(254, 183, 146, 0));
        matVisulize.writeMatToHistory(motionWithMatches, "motionWithMatches");
        return contours.getMatches();
    }

}
