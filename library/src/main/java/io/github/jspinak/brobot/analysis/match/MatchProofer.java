package io.github.jspinak.brobot.analysis.match;

import java.util.List;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;

/**
 * Validates whether matches fall within designated search regions.
 *
 * <p>Match proofing is a critical validation step that ensures found matches are actually within
 * the intended search areas. This becomes especially important when:
 *
 * <ul>
 *   <li>Search regions are non-rectangular or disconnected
 *   <li>Pattern matching algorithms return results outside search boundaries
 *   <li>Match fusion or adjustments move matches beyond original regions
 *   <li>Multiple overlapping search regions need consistent handling
 * </ul>
 *
 * <p>Different implementations offer trade-offs between accuracy and edge cases:
 *
 * <ul>
 *   <li>{@link RegionBasedProofer} - Strict containment within regions
 *   <li>{@link EdgeBasedProofer} - Edge-based validation for partial overlaps
 * </ul>
 *
 * @see RegionBasedProofer
 * @see EdgeBasedProofer
 * @see Region
 */
public interface MatchProofer {

    /**
     * Validates if a match is within the specified search regions.
     *
     * <p>This method addresses a fundamental challenge in region-based validation: determining
     * whether a match that may span multiple regions should be accepted. Different strategies
     * handle edge cases differently:
     *
     * <p><b>Strategy 1 - Strict Containment:</b>
     *
     * <ul>
     *   <li>Checks if the match is fully contained within at least one region
     *   <li>Pros: Ensures matches are completely within intended areas
     *   <li>Cons: May reject valid matches that span adjacent regions
     * </ul>
     *
     * <p><b>Strategy 2 - Edge Validation:</b>
     *
     * <ul>
     *   <li>Checks if all four edges of the match fall within search regions
     *   <li>Pros: Accepts matches spanning multiple adjacent regions
     *   <li>Cons: May accept matches in gaps between non-adjacent regions
     * </ul>
     *
     * @param match The match to validate against the search regions
     * @param regions The list of valid search regions. Matches outside these regions should be
     *     rejected.
     * @return true if the match is considered valid within the search regions according to the
     *     implementation's strategy, false otherwise
     */
    boolean isInSearchRegions(Match match, List<Region> regions);

    /**
     * Validates if a match is within search regions derived from action options and pattern.
     *
     * <p>This overloaded method handles the common case where search regions need to be determined
     * from multiple sources. It typically delegates to {@link SearchRegionResolver} to resolve the
     * appropriate regions based on priority rules, then validates the match against those regions.
     *
     * <p>The region selection follows a priority hierarchy:
     *
     * <ol>
     *   <li>Pattern's fixed regions (highest priority)
     *   <li>Configuration search regions
     *   <li>Pattern's standard search regions
     * </ol>
     *
     * @param match The match to validate
     * @param actionConfig Configuration that may override pattern regions
     * @param pattern The pattern that may define its own search regions
     * @return true if the match is valid within the resolved search regions, false otherwise
     * @see SearchRegionResolver#getRegions(ActionConfig, Pattern)
     */
    boolean isInSearchRegions(Match match, ActionConfig actionConfig, Pattern pattern);
}
