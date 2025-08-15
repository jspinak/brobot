package io.github.jspinak.brobot.action.internal.find.pixel;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.analysis.compare.ContourExtractor;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.action.internal.find.match.MatchCollectionUtilities;
import io.github.jspinak.brobot.model.analysis.color.ColorSchema;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.util.image.visualization.ScoringVisualizer;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.HSV;
import static io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis.Analysis.BGR_FROM_INDICES_2D;

/**
 * Extracts match regions from pixel-level color analysis results.
 * 
 * <p>PixelRegionExtractor converts pixel-wise classification and scoring data into
 * concrete Match objects representing contiguous regions of interest. It bridges
 * the gap between low-level pixel analysis and high-level pattern matching
 * results.</p>
 * 
 * <p>Key operations:</p>
 * <ul>
 *   <li>Identifies contiguous regions from classification results</li>
 *   <li>Filters regions by size constraints (minArea/maxArea)</li>
 *   <li>Respects search region boundaries</li>
 *   <li>Generates Match objects with location and scoring data</li>
 * </ul>
 * 
 * <p>The contour detection process uses the BGR visualization matrix
 * from scene analysis, where pixels are colored according to their
 * classified state image. This enables efficient region extraction
 * using OpenCV's contour finding algorithms.</p>
 * 
 * @see SceneAnalysis
 * @see ContourExtractor
 * @see SearchRegionResolver
 * @see MatchCollectionUtilities
 */
@Component
public class PixelRegionExtractor {

    private final SearchRegionResolver selectRegions;
    private final MatchCollectionUtilities matchOps;
    private final ScoringVisualizer showScoring;

    public PixelRegionExtractor(SearchRegionResolver selectRegions, MatchCollectionUtilities matchOps, ScoringVisualizer showScoring) {
        this.selectRegions = selectRegions;
        this.matchOps = matchOps;
        this.showScoring = showScoring;
    }

    /**
     * Finds match regions from scene analysis classification results.
     * 
     * <p>Processes the color classification data to identify contiguous
     * regions that match the target criteria. The method:</p>
     * <ol>
     *   <li>Gathers search regions from state image objects</li>
     *   <li>Extracts contours from the BGR classification visualization</li>
     *   <li>Filters contours by size constraints</li>
     *   <li>Converts contours to Match objects</li>
     * </ol>
     * 
     * <p>Side effects: Updates sceneAnalysis with contour data and may
     * trigger debug visualization if contours are found</p>
     *
     * @param sceneAnalysis contains classification results and scene data
     * @param actionConfig configuration including size constraints
     * @return ActionResult containing found matches
     */
    public ActionResult find(SceneAnalysis sceneAnalysis, ActionConfig actionConfig) {
        List<Region> searchRegions = new ArrayList<>();
        sceneAnalysis.getStateImageObjects().forEach(sio -> searchRegions.addAll(selectRegions.getRegions(actionConfig, sio)));
        ContourExtractor contours = new ContourExtractor.Builder()
                .setBgrFromClassification2d(sceneAnalysis.getAnalysis(BGR, BGR_FROM_INDICES_2D))
                .setSearchRegions(searchRegions)
                // Use default values since ActionConfig doesn't have these methods
                .setMinArea(1)
                .setMaxArea(-1)
                .build();
        List<Match> matchList = contours.getMatchList();
        if (!contours.getContours().isEmpty()) showScoring(contours, sceneAnalysis);
        ActionResult matches = new ActionResult();
        sceneAnalysis.setContours(contours);
        matchOps.addMatchListToMatches(matchList, matches);
        return matches;
    }

    /**
     * Displays debug information for the first contour found.
     * 
     * <p>Extracts the first pixel of the first contour and compares it
     * to the mean color values of the matched state image. Useful for
     * debugging color matching accuracy.</p>
     * 
     * <p>Side effects: Outputs visualization data to debug channels</p>
     * 
     * @param contours contains the detected contour regions
     * @param sceneAnalysis provides scene and color schema data
     */
    private void showScoring(ContourExtractor contours, SceneAnalysis sceneAnalysis) {
        Mat contour = contours.getMatchAsMatInScene(0, sceneAnalysis.getAnalysis(HSV, SceneAnalysis.Analysis.SCENE));
        Mat firstPixel = new Mat(contour, new Rect(0,0,1,1));
        ColorSchema colorSchema = sceneAnalysis.getStateImageObjects().get(0).getColorCluster().getSchema(HSV);
        showScoring.showPixelAndMean(firstPixel, colorSchema);
    }


}
