package io.github.jspinak.brobot.action.basic.find.motion;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.action.internal.find.match.MatchCollectionUtilities;
import io.github.jspinak.brobot.action.internal.find.scene.SceneAnalysisCollectionBuilder;
import io.github.jspinak.brobot.analysis.compare.ContourExtractor;
import io.github.jspinak.brobot.analysis.motion.MotionDetector;
import io.github.jspinak.brobot.analysis.motion.MovingObjectSelector;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis.Analysis.SCENE;

/**
 * Detects and tracks motion across multiple scene captures.
 * This class implements motion detection by analyzing changes between three
 * consecutive screenshots to identify moving objects and distinguish them
 * from the background.
 * 
 * <p>The motion detection algorithm requires three scenes:
 * <ul>
 * <li>Scene 1 and 2: Used to detect initial motion</li>
 * <li>Scene 2 and 3: Used to confirm motion and track trajectory</li>
 * <li>All 3 scenes: Used to distinguish moving objects from background changes</li>
 * </ul></p>
 * 
 * <p>Key features:
 * <ul>
 * <li>Identifies moving objects by tracking similar trajectories across scenes</li>
 * <li>Filters out background changes that appear stationary</li>
 * <li>Supports configurable time intervals between captures</li>
 * <li>Provides visual illustrations of detected motion</li>
 * </ul></p>
 * 
 * <p>The TimeBetweenIndividualActions option in ActionOptions controls
 * the time interval (in seconds) between screenshot captures.</p>
 * 
 * @see MotionDetector
 * @see MovingObjectSelector
 * @see VisualizeMotion
 * @see SceneAnalyses
 */
@Component
public class FindMotion {

    private final MotionDetector detectMotion;
    private final MatchCollectionUtilities matchOps;
    private final SceneAnalysisCollectionBuilder getSceneAnalysisCollection;
    private final MovingObjectSelector selectMovingObject;
    private final SearchRegionResolver selectRegions;
    private final VisualizeMotion illustrateMotion;

    /**
     * Constructs a FindMotion instance with all required dependencies.
     * 
     * @param detectMotion service for detecting pixel changes between images
     * @param matchOps utility for manipulating match collections
     * @param getSceneAnalysisCollection service for capturing and analyzing scenes
     * @param selectMovingObject service for identifying objects with consistent motion
     * @param selectRegions service for determining search regions
     * @param illustrateMotion service for creating visual motion representations
     */
    public FindMotion(MotionDetector detectMotion, MatchCollectionUtilities matchOps, SceneAnalysisCollectionBuilder getSceneAnalysisCollection,
                      MovingObjectSelector selectMovingObject, SearchRegionResolver selectRegions,
                      VisualizeMotion illustrateMotion) {
        this.detectMotion = detectMotion;
        this.matchOps = matchOps;
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
        this.selectMovingObject = selectMovingObject;
        this.selectRegions = selectRegions;
        this.illustrateMotion = illustrateMotion;
    }

    /**
     * Finds motion by analyzing changes across three consecutive scene captures.
     * This method implements a sophisticated motion detection algorithm that distinguishes
     * moving objects from background changes by tracking consistent trajectories.
     * 
     * <p>The algorithm works as follows:
     * <ul>
     * <li>Captures 3 scenes at specified time intervals</li>
     * <li>Detects changes between scenes 1-2 and scenes 2-3</li>
     * <li>Identifies objects with consistent motion trajectories</li>
     * <li>Filters out stationary background changes</li>
     * </ul></p>
     * 
     * <p>When images are provided in objectCollections, they can be used to track
     * specific objects. Without provided images, all detected motion is returned.
     * For screen motion detection, use empty or no ObjectCollection.</p>
     * 
     * <p>Side effects:
     * <ul>
     * <li>Populates matches with detected moving objects</li>
     * <li>Sets scene analysis collection in matches</li>
     * <li>Creates motion illustrations for each scene</li>
     * <li>Sorts matches by score in descending order</li>
     * </ul></p>
     * 
     * @param matches the {@link ActionResult} to populate with motion matches
     * @param objectCollections collections containing images to analyze (can be empty for screen capture)
     */
    public void find(ActionResult matches, List<ObjectCollection> objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        SceneAnalyses sceneAnalysisCollection = getSceneAnalysisCollection.get(
                objectCollections, 3, 0.1, actionOptions);
        if (sceneAnalysisCollection.getSceneAnalyses().size() < 3) {
            ConsoleReporter.println("Not enough scenes to detect motion");
            return;
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
            sA.getIllustrations().setMotion(sA.getScene().getPattern().getImage().getMatBGR());
            sA.getIllustrations().setMotionWithMatches(sA.getScene().getPattern().getImage().getMatBGR());
        });
        matchOps.addMatchListToMatches(movingObjects.get(2), matches);
        matches.sortByMatchScoreDecending();
        matchOps.limitNumberOfMatches(matches, actionOptions);
    }

    /**
     * Detects regions of change between two scenes and returns them as matches.
     * This method uses absolute difference to identify changed pixels, then
     * finds contours around these regions to create match objects.
     * 
     * <p>The process involves:
     * <ul>
     * <li>Computing absolute difference between two scenes</li>
     * <li>Finding contours in the difference image</li>
     * <li>Filtering contours by area and search region constraints</li>
     * <li>Creating match objects for valid motion regions</li>
     * </ul></p>
     * 
     * <p>Side effects:
     * <ul>
     * <li>Sets contours in the scene analysis collection</li>
     * <li>Creates motion illustrations for the second scene</li>
     * </ul></p>
     * 
     * @param sceneAnalysisCollection collection containing scenes to analyze
     * @param index1 index of the first scene in the collection
     * @param index2 index of the second scene in the collection
     * @param actionOptions provides min/max area constraints for valid contours
     * @param searchRegions list of regions to limit motion detection (empty = full scene)
     * @return list of {@link Match} objects representing detected motion regions
     * @see ContourExtractor
     * @see MotionDetector#getDynamicPixelMask
     */
    public List<Match> getRegionsOfChange(SceneAnalyses sceneAnalysisCollection, int index1, int index2,
                                           ActionOptions actionOptions, List<Region> searchRegions) {
        Mat scene1 = sceneAnalysisCollection.getSceneAnalyses().get(index1).getAnalysis(BGR, SCENE);
        Mat scene2 = sceneAnalysisCollection.getSceneAnalyses().get(index2).getAnalysis(BGR, SCENE);
        if (searchRegions.isEmpty()) searchRegions.add(new Region(0, 0, scene1.cols(), scene1.rows()));
        Mat absdiff = detectMotion.getDynamicPixelMask(scene1, scene2);
        ContourExtractor contours = new ContourExtractor.Builder()
                .setBgrFromClassification2d(absdiff) // absdiff works just as well for contours as the BGR_CLASSIFICATION_2D Mat would
                .setMinArea(actionOptions.getMinArea())
                .setMaxArea(actionOptions.getMaxArea())
                .setSearchRegions(searchRegions)
                .build();
        sceneAnalysisCollection.setContours(contours);
        illustrateMotion.setMotionMatAndWriteIllustration(sceneAnalysisCollection, index2, absdiff);
        return contours.getMatchList();
    }



}
