package io.github.jspinak.brobot.action.basic.find;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.internal.find.TargetImageMatchExtractor;
import io.github.jspinak.brobot.action.internal.find.match.MatchCollectionUtilities;
import io.github.jspinak.brobot.action.internal.find.scene.SceneAnalysisCollectionBuilder;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Executes color-based pattern matching and scene classification.
 *
 * <p>FindColor implements a sophisticated color-based matching system that can identify GUI
 * elements, classify scene regions, and detect state changes based on color patterns. It serves as
 * the primary interface for color-based operations in the Brobot framework.
 *
 * <p>Color matching workflow:
 *
 * <ol>
 *   <li>Acquire scenes (screenshot or provided images)
 *   <li>Gather classification images (targets and context)
 *   <li>Perform pixel-level color classification
 *   <li>Extract contiguous regions as match candidates
 *   <li>Filter and sort matches by size or score
 * </ol>
 *
 * <p>ObjectCollection usage:
 *
 * <ul>
 *   <li><b>First collection</b>: Target images to find (results as matches)
 *   <li><b>Second collection</b>: Context images for improved classification
 *   <li><b>Third collection</b>: Scenes to analyze (or screenshot if empty)
 * </ul>
 *
 * <p>Supports two primary modes:
 *
 * <ul>
 *   <li><b>FIND</b>: Locates specific patterns, sorted by similarity score
 *   <li><b>CLASSIFY</b>: Segments entire scene, sorted by region size
 * </ul>
 *
 * @see TargetImageMatchExtractor
 * @see SceneAnalysisCollectionBuilder
 * @see SceneAnalysis
 */
@Component
public class FindColor {

    private final TargetImageMatchExtractor getClassMatches;
    private final MatchCollectionUtilities matchOps;
    private final SceneAnalysisCollectionBuilder getSceneAnalysisCollection;

    public FindColor(
            TargetImageMatchExtractor getClassMatches,
            MatchCollectionUtilities matchOps,
            SceneAnalysisCollectionBuilder getSceneAnalysisCollection) {
        this.getClassMatches = getClassMatches;
        this.matchOps = matchOps;
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
    }

    /**
     * Performs color-based matching using provided image collections.
     *
     * <p>Processes three distinct ObjectCollections to configure the matching operation:
     *
     * <ol>
     *   <li><b>Target images</b> (first): Images to find and return as matches. Empty = classify
     *       only mode with no specific targets
     *   <li><b>Context images</b> (second): Additional images for classification accuracy. Empty =
     *       use all active state images. Targets are automatically included
     *   <li><b>Scenes</b> (third): Images to analyze. Empty = capture screenshot. Multiple scenes =
     *       multiple independent analyses
     * </ol>
     *
     * <p>Match sorting depends on action type:
     *
     * <ul>
     *   <li>CLASSIFY: Sorted by region size (largest first)
     *   <li>Others: Sorted by similarity score (highest first)
     * </ul>
     *
     * <p>Side effects: Updates matches with found regions and may capture screenshots
     *
     * @param matches contains action configuration and accumulates results
     * @param objectCollections three collections configuring the operation
     */
    public void find(ActionResult matches, List<ObjectCollection> objectCollections) {
        ActionConfig config = matches.getActionConfig();
        if (config instanceof ColorFindOptions) {
            findWithColorOptions(matches, objectCollections, (ColorFindOptions) config);
        } else {
            // Use default ColorFindOptions if not provided
            findWithColorOptions(
                    matches, objectCollections, new ColorFindOptions.Builder().build());
        }
    }

    private void findWithColorOptions(
            ActionResult matches,
            List<ObjectCollection> objectCollections,
            ColorFindOptions colorOptions) {
        if (colorOptions.getDiameter() < 0) return;
        Set<StateImage> targetImages =
                getSceneAnalysisCollection.getTargetImages(objectCollections);

        // getClassMatches accepts ActionConfig which ColorFindOptions extends
        ActionResult classMatches =
                getClassMatches.getMatches(
                        matches.getSceneAnalysisCollection(), targetImages, colorOptions);
        matches.addAllResults(classMatches);

        // For ColorFindOptions, we check the color strategy instead of action type
        if (colorOptions.getColor() == ColorFindOptions.Color.CLASSIFICATION) {
            matches.getMatchList().sort(Comparator.comparing(Match::size).reversed());
        } else {
            matches.getMatchList().sort(Comparator.comparingDouble(Match::getScore).reversed());
        }
        matchOps.limitNumberOfMatches(matches, colorOptions);
    }

    // These are no longer needed with ActionConfig removed

}
