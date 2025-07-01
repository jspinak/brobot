package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.internal.find.TargetImageMatchExtractor;
import io.github.jspinak.brobot.action.internal.find.match.MatchCollectionUtilities;
import io.github.jspinak.brobot.action.internal.find.scene.SceneAnalysisCollectionBuilder;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.match.Match;

import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Executes color-based pattern matching and scene classification.
 * 
 * <p>FindColor implements a sophisticated color-based matching system that can
 * identify GUI elements, classify scene regions, and detect state changes based
 * on color patterns. It serves as the primary interface for color-based operations
 * in the Brobot framework.</p>
 * 
 * <p>Color matching workflow:</p>
 * <ol>
 *   <li>Acquire scenes (screenshot or provided images)</li>
 *   <li>Gather classification images (targets and context)</li>
 *   <li>Perform pixel-level color classification</li>
 *   <li>Extract contiguous regions as match candidates</li>
 *   <li>Filter and sort matches by size or score</li>
 * </ol>
 * 
 * <p>ObjectCollection usage:</p>
 * <ul>
 *   <li><b>First collection</b>: Target images to find (results as matches)</li>
 *   <li><b>Second collection</b>: Context images for improved classification</li>
 *   <li><b>Third collection</b>: Scenes to analyze (or screenshot if empty)</li>
 * </ul>
 * 
 * <p>Supports two primary modes:</p>
 * <ul>
 *   <li><b>FIND</b>: Locates specific patterns, sorted by similarity score</li>
 *   <li><b>CLASSIFY</b>: Segments entire scene, sorted by region size</li>
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

    public FindColor(TargetImageMatchExtractor getClassMatches, MatchCollectionUtilities matchOps,
                     SceneAnalysisCollectionBuilder getSceneAnalysisCollection) {
        this.getClassMatches = getClassMatches;
        this.matchOps = matchOps;
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
    }

    /**
     * Performs color-based matching using provided image collections.
     * 
     * <p>Processes three distinct ObjectCollections to configure the matching operation:</p>
     * <ol>
     *   <li><b>Target images</b> (first): Images to find and return as matches.
     *       Empty = classify only mode with no specific targets</li>
     *   <li><b>Context images</b> (second): Additional images for classification accuracy.
     *       Empty = use all active state images. Targets are automatically included</li>
     *   <li><b>Scenes</b> (third): Images to analyze.
     *       Empty = capture screenshot. Multiple scenes = multiple independent analyses</li>
     * </ol>
     * 
     * <p>Match sorting depends on action type:</p>
     * <ul>
     *   <li>CLASSIFY: Sorted by region size (largest first)</li>
     *   <li>Others: Sorted by similarity score (highest first)</li>
     * </ul>
     * 
     * <p>Side effects: Updates matches with found regions and may capture screenshots</p>
     * 
     * @param matches contains action configuration and accumulates results
     * @param objectCollections three collections configuring the operation
     */
    public void find(ActionResult matches, List<ObjectCollection> objectCollections) {
        // Handle both ActionOptions (legacy) and ColorFindOptions (new)
        ActionConfig config = matches.getActionConfig();
        if (config instanceof ColorFindOptions) {
            findWithColorOptions(matches, objectCollections, (ColorFindOptions) config);
        } else if (config == null && matches.getActionOptions() != null) {
            // Legacy path for backward compatibility
            findWithActionOptions(matches, objectCollections, matches.getActionOptions());
        } else {
            throw new IllegalArgumentException("FindColor requires ColorFindOptions or ActionOptions");
        }
    }
    
    private void findWithColorOptions(ActionResult matches, List<ObjectCollection> objectCollections, ColorFindOptions colorOptions) {
        if (colorOptions.getDiameter() < 0) return;
        Set<StateImage> targetImages = getSceneAnalysisCollection.getTargetImages(objectCollections);
        
        // Convert ColorFindOptions to ActionOptions for now (until internal methods are updated)
        ActionOptions actionOptions = convertToActionOptions(colorOptions);
        ActionResult classMatches = getClassMatches.getMatches(matches.getSceneAnalysisCollection(), targetImages, actionOptions);
        matches.addAllResults(classMatches);
        
        // For ColorFindOptions, we check the color strategy instead of action type
        if (colorOptions.getColor() == ColorFindOptions.Color.CLASSIFICATION) {
            matches.getMatchList().sort(Comparator.comparing(Match::size).reversed());
        } else {
            matches.getMatchList().sort(Comparator.comparingDouble(Match::getScore).reversed());
        }
        matchOps.limitNumberOfMatches(matches, actionOptions);
    }
    
    private void findWithActionOptions(ActionResult matches, List<ObjectCollection> objectCollections, ActionOptions actionOptions) {
        if (actionOptions.getDiameter() < 0) return;
        Set<StateImage> targetImages = getSceneAnalysisCollection.getTargetImages(objectCollections);
        ActionResult classMatches = getClassMatches.getMatches(matches.getSceneAnalysisCollection(), targetImages, actionOptions);
        matches.addAllResults(classMatches);
        if (actionOptions.getAction() == ActionOptions.Action.CLASSIFY)
            matches.getMatchList().sort(Comparator.comparing(Match::size).reversed());
        else matches.getMatchList().sort(Comparator.comparingDouble(Match::getScore).reversed());
        matchOps.limitNumberOfMatches(matches, actionOptions);
    }
    
    private ActionOptions convertToActionOptions(ColorFindOptions colorOptions) {
        ActionOptions.Builder builder = new ActionOptions.Builder();
        builder.setDiameter(colorOptions.getDiameter());
        builder.setKmeans(colorOptions.getKmeans());
        builder.setSearchRegions(colorOptions.getSearchRegions());
        
        // Map color strategy to action type
        if (colorOptions.getColor() == ColorFindOptions.Color.CLASSIFICATION) {
            builder.setAction(ActionOptions.Action.CLASSIFY);
        } else {
            builder.setAction(ActionOptions.Action.FIND);
        }
        
        // Map other common properties from BaseFindOptions
        builder.setMinSimilarity(colorOptions.getSimilarity());
        builder.setMaxMatchesToActOn(colorOptions.getMaxMatchesToActOn());
        builder.setUseDefinedRegion(colorOptions.isUseDefinedRegion());
        
        // Map properties from ActionConfig
        builder.setPauseBeforeBegin(colorOptions.getPauseBeforeBegin());
        builder.setPauseAfterEnd(colorOptions.getPauseAfterEnd());
        
        return builder.build();
    }

}
