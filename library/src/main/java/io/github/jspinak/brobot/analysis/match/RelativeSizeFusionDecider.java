package io.github.jspinak.brobot.analysis.match;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.match.Match;

/**
 * Fusion strategy using size-relative distance thresholds for match grouping.
 *
 * <p>This implementation adapts fusion distances based on match sizes, making it more flexible than
 * absolute pixel thresholds. It calculates fusion distances as a percentage of the smaller match's
 * height, ensuring that:
 *
 * <ul>
 *   <li>Small UI elements use proportionally smaller fusion distances
 *   <li>Large UI elements allow proportionally larger gaps
 *   <li>Fusion behavior scales naturally with UI element sizes
 * </ul>
 *
 * <p><b>Algorithm:</b>
 *
 * <ol>
 *   <li>Determines the minimum height between two matches
 *   <li>Calculates X and Y fusion distances as percentages of this height
 *   <li>Delegates to absolute size fusion with the calculated distances
 * </ol>
 *
 * <p>This approach is particularly effective for:
 *
 * <ul>
 *   <li>Responsive UIs with varying element sizes
 *   <li>Multi-scale pattern matching
 *   <li>Applications with proportional spacing rules
 * </ul>
 *
 * @see MatchFusionDecider
 * @see AbsoluteSizeFusionDecider
 * @see MatchFusion
 */
@Component
public class RelativeSizeFusionDecider implements MatchFusionDecider {

    private final AbsoluteSizeFusionDecider matchFusionDeciderAbsoluteSize;

    /**
     * Creates a new relative size fusion decider.
     *
     * @param matchFusionDeciderAbsoluteSize The absolute size decider used to perform the actual
     *     fusion check with calculated distances
     */
    public RelativeSizeFusionDecider(AbsoluteSizeFusionDecider matchFusionDeciderAbsoluteSize) {
        this.matchFusionDeciderAbsoluteSize = matchFusionDeciderAbsoluteSize;
    }

    /**
     * Determines if two matches should be fused based on size-relative distance thresholds.
     *
     * <p>This method calculates adaptive fusion distances based on the smaller match's height,
     * ensuring that fusion behavior scales appropriately with UI element sizes. Using the minimum
     * height prevents large elements from creating excessive fusion zones that could incorrectly
     * merge with distant smaller elements.
     *
     * <p><b>Example:</b> With xDistAsPercentOfMatchHeight=50 and yDistAsPercentOfMatchHeight=50:
     *
     * <ul>
     *   <li>Two 20px tall buttons: fusion distances = 10px (50% of 20px)
     *   <li>Two 100px tall panels: fusion distances = 50px (50% of 100px)
     *   <li>20px button and 100px panel: fusion distances = 10px (50% of smaller height)
     * </ul>
     *
     * <p><b>Note:</b> The parameters are interpreted as percentages despite their int type. This is
     * a legacy design decision maintained for backward compatibility.
     *
     * @param match The first match to evaluate for fusion
     * @param match2 The second match to evaluate for fusion
     * @param xDistAsPercentOfMatchHeight The horizontal fusion distance as a percentage of the
     *     smaller match's height (e.g., 50 = 50%)
     * @param yDistAsPercentOfMatchHeight The vertical fusion distance as a percentage of the
     *     smaller match's height (e.g., 50 = 50%)
     * @return true if the matches should be fused based on the calculated relative distances, false
     *     if they should remain separate
     */
    @Override
    public boolean isSameMatchGroup(
            Match match,
            Match match2,
            int xDistAsPercentOfMatchHeight,
            int yDistAsPercentOfMatchHeight) {
        int averageHeight = (match.h() + match2.h()) / 2;
        int maxHeight = Math.max(match.h(), match2.h());
        int minHeight = Math.min(match.h(), match2.h());
        int minXDist = minHeight * xDistAsPercentOfMatchHeight / 100;
        int minYDist = minHeight * yDistAsPercentOfMatchHeight / 100;
        return matchFusionDeciderAbsoluteSize.isSameMatchGroup(match, match2, minXDist, minYDist);
    }
}
