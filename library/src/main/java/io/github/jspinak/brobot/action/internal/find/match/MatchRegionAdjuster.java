package io.github.jspinak.brobot.action.internal.find.match;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;

/**
 * Adjusts the position and dimensions of Match objects based on MatchAdjustmentOptions settings.
 *
 * <p>This component modifies Match regions by applying additive or absolute adjustments specified
 * in MatchAdjustmentOptions. It supports both individual match adjustment and batch processing of
 * multiple matches. The adjustments can be used to:
 *
 * <ul>
 *   <li>Offset match positions (using addX/addY)
 *   <li>Resize matches relatively (using addW/addH)
 *   <li>Set absolute dimensions (using absoluteW/absoluteH)
 *   <li>Apply target position and offset adjustments
 * </ul>
 *
 * <p>This is version 2 of the MatchAdjuster, updated to work with the new ActionConfig hierarchy
 * and MatchAdjustmentOptions instead of ActionConfig.
 *
 * @see Match
 * @see MatchAdjustmentOptions
 * @see ActionResult
 * @since 2.0
 */
@Component
public class MatchRegionAdjuster {

    // No longer needs legacy dependency - this class is now standalone

    /**
     * Adjusts a single match's region based on the specified adjustment options.
     *
     * <p>This method modifies the match's region in-place by applying position offsets and
     * dimension adjustments. Absolute dimensions take precedence over relative adjustments when
     * both are specified.
     *
     * @param match The match whose region will be adjusted. This object is modified in-place.
     * @param adjustmentOptions Configuration specifying the adjustments to apply: - addX/addY:
     *     Pixel offsets for position - addW/addH: Pixel adjustments for dimensions -
     *     absoluteW/absoluteH: Absolute dimension values (overrides additive) - targetPosition:
     *     Position within match to target - targetOffset: Additional offset from target position
     */
    public void adjust(Match match, MatchAdjustmentOptions adjustmentOptions) {
        if (adjustmentOptions == null) {
            return;
        }

        // Apply position adjustments
        match.getRegion().setX(match.x() + adjustmentOptions.getAddX());
        match.getRegion().setY(match.y() + adjustmentOptions.getAddY());

        // Apply dimension adjustments (absolute takes precedence)
        if (adjustmentOptions.getAbsoluteW() > 0) {
            match.getRegion().setW(adjustmentOptions.getAbsoluteW());
        } else {
            match.getRegion().setW(match.w() + adjustmentOptions.getAddW());
        }

        if (adjustmentOptions.getAbsoluteH() > 0) {
            match.getRegion().setH(adjustmentOptions.getAbsoluteH());
        } else {
            match.getRegion().setH(match.h() + adjustmentOptions.getAddH());
        }

        // Apply target position if specified
        if (adjustmentOptions.getTargetPosition() != null) {
            if (match.getTarget() == null) {
                match.setTarget(new Location(match.getRegion()));
            }
            match.getTarget().setPosition(adjustmentOptions.getTargetPosition());
        }

        // Apply target offset if specified
        if (adjustmentOptions.getTargetOffset() != null) {
            if (match.getTarget() == null) {
                match.setTarget(new Location(match.getRegion()));
            }
            match.getTarget().setOffsetX(adjustmentOptions.getTargetOffset().getX());
            match.getTarget().setOffsetY(adjustmentOptions.getTargetOffset().getY());
        }
    }

    /**
     * Adjusts all matches within an ActionResult based on the specified adjustment options.
     *
     * <p>This method applies the same adjustments to every match in the result set, modifying each
     * match's region in-place.
     *
     * @param matches The ActionResult containing matches to adjust. All matches within this object
     *     are modified.
     * @param adjustmentOptions Configuration specifying the adjustments to apply to each match
     */
    public void adjustAll(ActionResult matches, MatchAdjustmentOptions adjustmentOptions) {
        if (adjustmentOptions == null || matches == null) {
            return;
        }
        matches.getMatchList().forEach(match -> adjust(match, adjustmentOptions));
    }

    /**
     * Adjusts all matches contained within an ObjectCollection.
     *
     * <p>This method processes all ActionResult objects in the collection, applying the specified
     * adjustments to every match found within them.
     *
     * @param objectCollection The collection containing ActionResults with matches to adjust. All
     *     matches within all ActionResults are modified.
     * @param adjustmentOptions Configuration specifying the adjustments to apply to each match
     */
    public void adjustAll(
            ObjectCollection objectCollection, MatchAdjustmentOptions adjustmentOptions) {
        if (adjustmentOptions == null || objectCollection == null) {
            return;
        }
        objectCollection.getMatches().forEach(matches -> adjustAll(matches, adjustmentOptions));
    }

    /**
     * Filters matches by minimum area, removing matches smaller than the specified threshold.
     *
     * <p>This method is integrated into the match adjustment pipeline to avoid redundant iterations
     * over the match list. It modifies the ActionResult's match list in-place, removing any matches
     * whose area (width × height) is less than the minimum.
     *
     * <p>This filtering is particularly useful for:
     *
     * <ul>
     *   <li>Removing noise and false positives from pattern matching
     *   <li>Ensuring matches meet minimum size requirements for interaction
     *   <li>Cleaning up results when searching for UI elements with expected dimensions
     * </ul>
     *
     * @param matches The ActionResult containing matches to filter. The match list within this
     *     object is modified in-place.
     * @param minArea The minimum area (in pixels²) a match must have to be retained. Matches with
     *     area less than this value are removed.
     */
    public void filterByMinimumArea(ActionResult matches, int minArea) {
        if (matches == null || minArea <= 0) {
            return;
        }

        List<Match> filteredMatches = new ArrayList<>();
        for (Match match : matches.getMatchList()) {
            int area = match.w() * match.h();
            if (area >= minArea) {
                filteredMatches.add(match);
            }
        }

        // Update the match list with filtered results
        matches.setMatchList(filteredMatches);
    }

    /**
     * Adjusts all matches and optionally filters by minimum area in a single pass.
     *
     * <p>This method combines position/dimension adjustments with area filtering to minimize
     * iterations over the match list. It's more efficient than calling adjustAll and
     * filterByMinimumArea separately.
     *
     * @param matches The ActionResult containing matches to process
     * @param adjustmentOptions Configuration for position and dimension adjustments
     * @param minArea Minimum area filter (0 or negative to disable filtering)
     */
    public void adjustAndFilter(
            ActionResult matches, MatchAdjustmentOptions adjustmentOptions, int minArea) {
        if (matches == null) {
            return;
        }

        if (minArea <= 0) {
            // No filtering needed, just adjust
            adjustAll(matches, adjustmentOptions);
            return;
        }

        List<Match> filteredMatches = new ArrayList<>();

        for (Match match : matches.getMatchList()) {
            // Apply adjustments if specified
            if (adjustmentOptions != null) {
                adjust(match, adjustmentOptions);
            }

            // Check area after adjustment
            int area = match.w() * match.h();
            if (area >= minArea) {
                filteredMatches.add(match);
            }
        }

        // Update the match list with adjusted and filtered results
        matches.setMatchList(filteredMatches);
    }
}
