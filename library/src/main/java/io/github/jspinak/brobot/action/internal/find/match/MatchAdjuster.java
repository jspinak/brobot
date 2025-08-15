package io.github.jspinak.brobot.action.internal.find.match;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.match.Match;

import org.springframework.stereotype.Component;

/**
 * Adjusts the position and dimensions of Match objects based on ActionOptions settings.
 * <p>
 * This component modifies Match regions by applying additive or absolute adjustments
 * specified in ActionOptions. It supports both individual match adjustment and batch
 * processing of multiple matches. The adjustments can be used to:
 * <ul>
 * <li>Offset match positions (using addX/addY)</li>
 * <li>Resize matches relatively (using addW/addH)</li>
 * <li>Set absolute dimensions (using absoluteW/absoluteH)</li>
 * </ul>
 * 
 * @see Match
 * @see ActionOptions
 * @see ActionResult
 */
@Component
public class MatchAdjuster {

    /**
     * Adjusts a single match's region based on the specified action options.
     * <p>
     * In Brobot 1.1.0+, region adjustments are typically handled through
     * SearchRegionOnObject or specific action configurations. This method
     * is maintained for compatibility but performs no adjustments in the
     * modern API.
     * 
     * @param match The match whose region will be adjusted. This object is modified
     *              in-place.
     * @param actionConfig Configuration specifying the adjustments to apply
     */
    public void adjust(Match match, ActionConfig actionConfig) {
        // In modern Brobot, adjustments are handled differently
        // Region adjustments would be specified in SearchRegionOnObject
        // or through specific action configurations
        // This method is retained for compatibility but performs no operations
    }

    /**
     * Adjusts all matches within an ActionResult based on the specified action options.
     * <p>
     * This method applies the same adjustments to every match in the result set,
     * modifying each match's region in-place.
     * 
     * @param matches The ActionResult containing matches to adjust. All matches
     *                within this object are modified.
     * @param actionConfig Configuration specifying the adjustments to apply to each match
     */
    public void adjustAll(ActionResult matches, ActionConfig actionConfig) {
        matches.getMatchList().forEach(match -> adjust(match, actionConfig));
    }

    /**
     * Adjusts all matches contained within an ObjectCollection.
     * <p>
     * This method processes all ActionResult objects in the collection, applying
     * the specified adjustments to every match found within them.
     * 
     * @param objectCollection The collection containing ActionResults with matches to adjust.
     *                        All matches within all ActionResults are modified.
     * @param actionConfig Configuration specifying the adjustments to apply to each match
     */
    public void adjustAll(ObjectCollection objectCollection, ActionConfig actionConfig) {
        objectCollection.getMatches().forEach(matches -> adjustAll(matches, actionConfig));
    }
}
