package io.github.jspinak.brobot.action.internal.find;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.PixelProfiles.Analysis.SCORE_DIST_BELOW_THRESHHOLD;
import static io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis.Analysis.BGR_FROM_INDICES_2D_TARGETS;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.internal.find.match.MatchCollectionUtilities;
import io.github.jspinak.brobot.analysis.compare.ContourExtractor;
import io.github.jspinak.brobot.model.analysis.color.PixelProfiles;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Extracts matches for specific target images from scene classification results.
 *
 * <p>TargetImageMatchExtractor processes scene analysis data to find concrete match regions for
 * requested state images. Unlike general classification which assigns every pixel, this component
 * focuses on extracting matches only for specific targets of interest.
 *
 * <p>Key capabilities:
 *
 * <ul>
 *   <li>Filters classification results to target images only
 *   <li>Respects search regions for each state image
 *   <li>Applies score thresholds to ensure match quality
 *   <li>Handles multiple scenes in a collection
 * </ul>
 *
 * <p>The matching process leverages pre-computed classification indices and score matrices to
 * efficiently extract regions corresponding to target state images, making it suitable for
 * selective pattern matching scenarios.
 *
 * @see SceneAnalyses
 * @see SceneAnalysis
 * @see ContourExtractor
 * @see SearchRegionResolver
 */
@Component
public class TargetImageMatchExtractor {

    private SearchRegionResolver selectRegions;
    private MatchCollectionUtilities matchOps;

    public TargetImageMatchExtractor(
            SearchRegionResolver selectRegions, MatchCollectionUtilities matchOps) {
        this.selectRegions = selectRegions;
        this.matchOps = matchOps;
    }

    /**
     * Finds matches for specific target images within classified scenes.
     *
     * <p>Processes a collection of scene analyses to extract match regions corresponding to the
     * requested target images. Each scene is analyzed independently, with results aggregated into a
     * single ActionResult.
     *
     * @param sceneAnalysisCollection contains classified scenes to search
     * @param targetImages the specific state images to find matches for
     * @param actionConfig configuration including size and score constraints
     * @return ActionResult containing all matches found across all scenes
     */
    public ActionResult getMatches(
            SceneAnalyses sceneAnalysisCollection,
            Set<StateImage> targetImages,
            ActionConfig actionConfig) {
        ActionResult matches = new ActionResult();
        List<SceneAnalysis> sceneAnalyses = sceneAnalysisCollection.getSceneAnalyses();
        if (sceneAnalyses.isEmpty()) return matches;
        for (SceneAnalysis sceneAnalysis : sceneAnalyses) {
            ActionResult matchesForScene =
                    getMatchesForOneScene(sceneAnalysis, targetImages, actionConfig);
            matches.addAllResults(matchesForScene);
        }
        return matches;
    }

    /**
     * Extracts matches for target images from a single scene analysis.
     *
     * <p>For each target image:
     *
     * <ol>
     *   <li>Retrieves appropriate search regions
     *   <li>Gets BGR visualization of target classifications
     *   <li>Extracts score matrices for quality filtering
     *   <li>Builds contours using score thresholds
     *   <li>Converts contours to Match objects
     * </ol>
     *
     * <p>Side effects: Updates sceneAnalysis with contour data
     *
     * @param sceneAnalysis the analyzed scene with classification results
     * @param targetImages the state images to find matches for
     * @param actionConfig configuration for match extraction
     * @return ActionResult containing matches for this scene
     */
    private ActionResult getMatchesForOneScene(
            SceneAnalysis sceneAnalysis, Set<StateImage> targetImages, ActionConfig actionConfig) {
        ActionResult matches = new ActionResult();
        for (StateImage sio : targetImages) {
            List<Region> searchRegions = selectRegions.getRegions(actionConfig, sio);
            Mat resultsInColor = sceneAnalysis.getAnalysis(BGR, BGR_FROM_INDICES_2D_TARGETS);
            Mat scoresMat = sceneAnalysis.getScoresMat(sio);
            Optional<PixelProfiles> pixelAnalysisCollection =
                    sceneAnalysis.getPixelAnalysisCollection(sio);
            if (pixelAnalysisCollection.isPresent()) {
                ContourExtractor contours =
                        new ContourExtractor.Builder()
                                .setScoreThresholdDist(
                                        pixelAnalysisCollection
                                                .get()
                                                .getAnalysis(SCORE_DIST_BELOW_THRESHHOLD, BGR))
                                .setScores(scoresMat)
                                .setBgrFromClassification2d(resultsInColor)
                                // Use default min/max area since ActionConfig doesn't have these
                                // methods
                                .setMinArea(10)
                                .setMaxArea(Integer.MAX_VALUE)
                                .setSearchRegions(searchRegions)
                                .build();
                sceneAnalysis.setContours(contours);
                matches.addSceneAnalysis(sceneAnalysis);
                List<Match> matchList = contours.getMatchList();
                matchOps.addMatchListToMatches(matchList, matches);
            }
        }
        return matches;
    }
}
