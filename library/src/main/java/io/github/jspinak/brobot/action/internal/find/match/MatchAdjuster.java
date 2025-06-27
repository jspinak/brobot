package io.github.jspinak.brobot.action.internal.find.match;

import io.github.jspinak.brobot.action.ActionOptions;
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
     * This method modifies the match's region in-place by applying position offsets
     * and dimension adjustments. Absolute dimensions take precedence over relative
     * adjustments when both are specified.
     * 
     * @param match The match whose region will be adjusted. This object is modified
     *              in-place.
     * @param actionOptions Configuration specifying the adjustments to apply:
     *                      - addX/addY: Pixel offsets for position
     *                      - addW/addH: Pixel adjustments for dimensions
     *                      - absoluteW/absoluteH: Absolute dimension values (overrides additive)
     */
    public void adjust(Match match, ActionOptions actionOptions) {
        match.getRegion().setX(match.x() + actionOptions.getAddX());
        match.getRegion().setY(match.y() + actionOptions.getAddY());
        if (actionOptions.getAbsoluteW() > 0) match.getRegion().setW(actionOptions.getAbsoluteW());
        else match.getRegion().setW(match.w() + actionOptions.getAddW());
        if (actionOptions.getAbsoluteH() > 0) match.getRegion().setH(actionOptions.getAbsoluteH());
        else match.getRegion().setH(match.h() + actionOptions.getAddH());
    }

    /**
     * Adjusts all matches within an ActionResult based on the specified action options.
     * <p>
     * This method applies the same adjustments to every match in the result set,
     * modifying each match's region in-place.
     * 
     * @param matches The ActionResult containing matches to adjust. All matches
     *                within this object are modified.
     * @param actionOptions Configuration specifying the adjustments to apply to each match
     */
    public void adjustAll(ActionResult matches, ActionOptions actionOptions) {
        matches.getMatchList().forEach(match -> adjust(match, actionOptions));
    }

    /**
     * Adjusts all matches contained within an ObjectCollection.
     * <p>
     * This method processes all ActionResult objects in the collection, applying
     * the specified adjustments to every match found within them.
     * 
     * @param objectCollection The collection containing ActionResults with matches to adjust.
     *                        All matches within all ActionResults are modified.
     * @param actionOptions Configuration specifying the adjustments to apply to each match
     */
    public void adjustAll(ObjectCollection objectCollection, ActionOptions actionOptions) {
        objectCollection.getMatches().forEach(matches -> adjustAll(matches, actionOptions));
    }
}
