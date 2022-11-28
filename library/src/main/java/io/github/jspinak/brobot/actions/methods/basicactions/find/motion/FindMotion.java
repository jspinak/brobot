package io.github.jspinak.brobot.actions.methods.basicactions.find.motion;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.MatchOps;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetSceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.contours.Contours;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis.Analysis.BGR_FROM_INDICES_2D;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;
import static org.bytedeco.opencv.global.opencv_core.add;
import static org.bytedeco.opencv.global.opencv_core.multiply;

/**
 * TimeBetweenIndividualActions is the option in ActionOptions that gives the time (in seconds)
 * between the first and second screenshots. These 2 screenshots will determine where motion
 * has occurred.
 */
@Component
public class FindMotion {

    private DetectMotion detectMotion;
    private MatchOps matchOps;
    private GetSceneAnalysisCollection getSceneAnalysisCollection;

    public FindMotion(DetectMotion detectMotion, MatchOps matchOps,
                      GetSceneAnalysisCollection getSceneAnalysisCollection) {
        this.detectMotion = detectMotion;
        this.matchOps = matchOps;
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
    }

    /**
     * When images are passed, motion between the images will be returned. Images must be of the same size.
     * To find motion on the screen, call Find.MOTION without an ObjectCollection or with an empty ObjectCollection.
     *
     * @param actionOptions The action's configuration
     * @param objectCollections holds the images to analyze for motion in the first collection
     * @return a Matches object with matches for objects that have moved.
     */
    public Matches find(ActionOptions actionOptions, List<ObjectCollection> objectCollections) {
        Matches matches = new Matches();
        SceneAnalysisCollection sceneAnalysisCollection = getSceneAnalysisCollection.get(
                objectCollections, 2, 1, actionOptions);
        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
        if (sceneAnalysisCollection.getSceneAnalyses().size() < 2) return matches; // nothing to compare
        // the following code finds movement, but not necessarily the images provided
        Mat absdiff = detectMotion.getAbsdiff(
                sceneAnalysisCollection.getSceneAnalyses().get(0)
                        .getAnalysis(BGR, SceneAnalysis.Analysis.SCENE),
                sceneAnalysisCollection.getSceneAnalyses().get(1)
                        .getAnalysis(BGR, SceneAnalysis.Analysis.SCENE));
        matches.setPixelMatches(absdiff);
        Mat scores = new Mat();
        multiply(new Mat(new Scalar(-1)), absdiff, scores);
        add(scores, new Mat(new Scalar(100)), scores); // scores is high when there is little movement
        Contours contours = new Contours.Builder()
                .setBgrFromClassification2d(
                        sceneAnalysisCollection.getSceneAnalyses().get(0).getAnalysis(BGR, BGR_FROM_INDICES_2D))
                .setMinArea(actionOptions.getMinArea())
                .setMaxArea(actionOptions.getMaxArea())
                .build();
        List<Match> matchList = contours.getMatches();
        matchOps.addGenericMatchObjects(matchList, matches, actionOptions);
        matches.sortByMatchScoreDecending();
        matchOps.limitNumberOfMatches(matches, actionOptions);
        return matches;
    }

    private double convertScoreToMovement(double score) {
        return 100 * (1 - score); // a high score is more selective, and will capture only where there is a lot of movement
    }

    private double convertMovementToScore(double movement) {
        return 1 - movement / 100;
    }

}
