package io.github.jspinak.brobot.action.internal.find;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.model.analysis.color.PixelProfiles;
import io.github.jspinak.brobot.analysis.compare.ContourExtractor;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.internal.find.match.MatchCollectionUtilities;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.jspinak.brobot.model.analysis.color.PixelProfiles.Analysis.SCORE_DIST_BELOW_THRESHHOLD;
import static io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis.Analysis.BGR_FROM_INDICES_2D_TARGETS;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;

/**
 * Extracts matches for specific target images from scene classification results.
 * 
 * <p>TargetImageMatchExtractor processes scene analysis data to find concrete match regions
 * for requested state images. Unlike general classification which assigns every
 * pixel, this component focuses on extracting matches only for specific targets
 * of interest.</p>
 * 
 * <p>Key capabilities:</p>
 * <ul>
 *   <li>Filters classification results to target images only</li>
 *   <li>Respects search regions for each state image</li>
 *   <li>Applies score thresholds to ensure match quality</li>
 *   <li>Handles multiple scenes in a collection</li>
 * </ul>
 * 
 * <p>The matching process leverages pre-computed classification indices and
 * score matrices to efficiently extract regions corresponding to target
 * state images, making it suitable for selective pattern matching scenarios.</p>
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

    public TargetImageMatchExtractor(SearchRegionResolver selectRegions, MatchCollectionUtilities matchOps) {
        this.selectRegions = selectRegions;
        this.matchOps = matchOps;
    }

    /**
     * Finds matches for specific target images within classified scenes.
     * 
     * <p>Processes a collection of scene analyses to extract match regions
     * corresponding to the requested target images. Each scene is analyzed
     * independently, with results aggregated into a single ActionResult.</p>
     * 
     * @param sceneAnalysisCollection contains classified scenes to search
     * @param targetImages the specific state images to find matches for
     * @param actionOptions configuration including size and score constraints
     * @return ActionResult containing all matches found across all scenes
     */
    public ActionResult getMatches(SceneAnalyses sceneAnalysisCollection, Set<StateImage> targetImages,
                              ActionOptions actionOptions) {
        ActionResult matches = new ActionResult();
        List<SceneAnalysis> sceneAnalyses = sceneAnalysisCollection.getSceneAnalyses();
        if (sceneAnalyses.isEmpty()) return matches;
        for (SceneAnalysis sceneAnalysis : sceneAnalyses) {
            ActionResult matchesForScene = getMatchesForOneScene(sceneAnalysis, targetImages, actionOptions);
            matches.addAllResults(matchesForScene);
        }
        return matches;
    }

    /**
     * Extracts matches for target images from a single scene analysis.
     * 
     * <p>For each target image:</p>
     * <ol>
     *   <li>Retrieves appropriate search regions</li>
     *   <li>Gets BGR visualization of target classifications</li>
     *   <li>Extracts score matrices for quality filtering</li>
     *   <li>Builds contours using score thresholds</li>
     *   <li>Converts contours to Match objects</li>
     * </ol>
     * 
     * <p>Side effects: Updates sceneAnalysis with contour data</p>
     * 
     * @param sceneAnalysis the analyzed scene with classification results
     * @param targetImages the state images to find matches for
     * @param actionOptions configuration for match extraction
     * @return ActionResult containing matches for this scene
     */
    private ActionResult getMatchesForOneScene(SceneAnalysis sceneAnalysis, Set<StateImage> targetImages,
                                          ActionOptions actionOptions) {
        ActionResult matches = new ActionResult();
        for (StateImage sio : targetImages) {
            List<Region> searchRegions = selectRegions.getRegions(actionOptions, sio);
            Mat resultsInColor = sceneAnalysis.getAnalysis(BGR, BGR_FROM_INDICES_2D_TARGETS);
            Mat scoresMat = sceneAnalysis.getScoresMat(sio);
            Optional<PixelProfiles> pixelAnalysisCollection = sceneAnalysis.getPixelAnalysisCollection(sio);
            if (pixelAnalysisCollection.isPresent()) {
                ContourExtractor contours = new ContourExtractor.Builder()
                        .setScoreThresholdDist(pixelAnalysisCollection.get().getAnalysis(SCORE_DIST_BELOW_THRESHHOLD, BGR))
                        .setScores(scoresMat)
                        .setBgrFromClassification2d(resultsInColor)
                        .setMinArea(actionOptions.getMinArea())
                        .setMaxArea(actionOptions.getMaxArea())
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
