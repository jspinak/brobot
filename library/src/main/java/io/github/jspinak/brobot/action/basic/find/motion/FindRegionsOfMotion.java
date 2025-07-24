package io.github.jspinak.brobot.action.basic.find.motion;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.action.internal.find.match.MatchCollectionUtilities;
import io.github.jspinak.brobot.action.internal.find.scene.SceneAnalysisCollectionBuilder;
import io.github.jspinak.brobot.analysis.compare.ContourExtractor;
import io.github.jspinak.brobot.analysis.motion.FindDynamicPixels;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.image.visualization.MatrixVisualizer;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Identifies regions containing motion across multiple scene captures.
 * This class analyzes a collection of scenes to find areas where any
 * pixel movement occurs, creating a comprehensive map of all motion regions.
 * 
 * <p>Unlike {@link FindMotion} which tracks specific moving objects,
 * this class identifies any region that experiences movement across
 * the entire observation period. This makes it useful for:
 * <ul>
 * <li>Detecting areas of activity in user interfaces</li>
 * <li>Identifying animated or dynamic screen regions</li>
 * <li>Monitoring for any changes in specific areas</li>
 * <li>Creating heat maps of screen activity</li>
 * </ul></p>
 * 
 * <p>The number of scenes and pause between captures are controlled by:
 * <ul>
 * <li>TimesToRepeatIndividualAction: number of scenes to capture</li>
 * <li>PauseBetweenIndividualActions: delay between captures (seconds)</li>
 * </ul></p>
 * 
 * @see FindDynamicPixels
 * @see SceneAnalyses
 * @see SceneAnalysisCollectionBuilder
 */
@Component
public class FindRegionsOfMotion {

    private final SceneAnalysisCollectionBuilder getSceneAnalysisCollection;
    private final SearchRegionResolver selectRegions;
    private final FindDynamicPixels findDynamicPixels;
    private final MatchCollectionUtilities matchOps;
    private final MatrixVisualizer matVisualize;

    /**
     * Constructs a FindRegionsOfMotion instance with required dependencies.
     * 
     * @param getSceneAnalysisCollection service for capturing and analyzing scenes
     * @param selectRegions service for determining search regions
     * @param findDynamicPixels service for detecting pixel changes
     * @param matchOps utility for match list operations
     * @param matVisualize utility for visualizing and saving images
     */
    public FindRegionsOfMotion(SceneAnalysisCollectionBuilder getSceneAnalysisCollection,
                               SearchRegionResolver selectRegions, FindDynamicPixels findDynamicPixels,
                               MatchCollectionUtilities matchOps, MatrixVisualizer matVisualize) {
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
        this.selectRegions = selectRegions;
        this.findDynamicPixels = findDynamicPixels;
        this.matchOps = matchOps;
        this.matVisualize = matVisualize;
    }

    /**
     * Finds all regions containing motion across multiple scene captures.
     * Captures scenes at specified intervals and identifies any areas where
     * pixels change between captures.
     * 
     * <p>The process involves:
     * <ul>
     * <li>Capturing multiple scenes based on action options</li>
     * <li>Detecting all pixels that change across any scenes</li>
     * <li>Finding contours around dynamic pixel regions</li>
     * <li>Creating matches for each motion region found</li>
     * </ul></p>
     * 
     * <p>Side effects:
     * <ul>
     * <li>Sets scene analysis collection in matches</li>
     * <li>Populates matches with all motion regions found</li>
     * <li>Creates motion illustrations for each scene</li>
     * <li>Sets the dynamic pixel mask in matches</li>
     * <li>Limits matches based on action options</li>
     * </ul></p>
     * 
     * @param matches the {@link ActionResult} to populate with motion regions
     * @param objectCollections not used by this implementation (can be empty)
     */
    public void find(ActionResult matches, List<ObjectCollection> objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        int scenes = actionOptions.getTimesToRepeatIndividualAction();
        double pause = actionOptions.getPauseBetweenIndividualActions();
        SceneAnalyses sceneAnalysisCollection = getSceneAnalysisCollection.get(
                objectCollections, scenes, pause, actionOptions);
        if (sceneAnalysisCollection.getSceneAnalyses().size() < 2) {
            ConsoleReporter.println("Not enough scenes to detect motion");
            return;
        }
        //System.out.println("FindRegionsOfMotion: # scenes = " + sceneAnalysisCollection.getSceneAnalyses().size());
        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
        List<Region> searchRegions = selectRegions.getRegionsForAllImages(actionOptions, objectCollections.toArray(new ObjectCollection[0]));
        List<Match> dynamicPixelRegions = getDynamicRegions(sceneAnalysisCollection, actionOptions, searchRegions);
        matches.getSceneAnalysisCollection().getSceneAnalyses().forEach(sA -> {
            sA.setMatchList(dynamicPixelRegions);
            sA.getIllustrations().setMotion(sceneAnalysisCollection.getResults());
            sA.getIllustrations().setMotionWithMatches(sA.getScene().getPattern().getImage().getMatBGR());
        });
        matchOps.addMatchListToMatches(dynamicPixelRegions, matches); // this is for the last scene
        matchOps.limitNumberOfMatches(matches, actionOptions);
        matches.setMask(sceneAnalysisCollection.getResults()); // pixelMatches = dynamic pixels
    }

    /**
     * Identifies regions containing dynamic pixels across all scenes.
     * Creates a cumulative mask showing all areas that experienced motion
     * at any point during the observation period.
     * 
     * <p>The method:
     * <ul>
     * <li>Combines all scenes to find any pixels that changed</li>
     * <li>Creates contours around dynamic pixel regions</li>
     * <li>Filters contours by area and search region constraints</li>
     * <li>Saves dynamic pixel visualization to history</li>
     * </ul></p>
     * 
     * <p>Side effects:
     * <ul>
     * <li>Sets results (dynamic pixel mask) in scene analysis collection</li>
     * <li>Sets contours in scene analysis collection</li>
     * <li>Writes dynamic pixel mask to history folder</li>
     * </ul></p>
     * 
     * @param sceneAnalysisCollection collection of scenes to analyze
     * @param actionOptions provides min/max area constraints for contours
     * @param searchRegions regions to limit motion detection (empty = full scene)
     * @return list of {@link Match} objects for each motion region found
     * @see FindDynamicPixels#getDynamicPixelMask
     * @see ContourExtractor
     */
    public List<Match> getDynamicRegions(SceneAnalyses sceneAnalysisCollection,
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
        ContourExtractor contours = new ContourExtractor.Builder()
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
