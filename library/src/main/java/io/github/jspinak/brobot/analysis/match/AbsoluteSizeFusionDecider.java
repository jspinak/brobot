package io.github.jspinak.brobot.analysis.match;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Fusion strategy using fixed pixel distance thresholds for match grouping.
 * <p>
 * This implementation determines whether matches should be fused based on absolute
 * pixel distances, regardless of the match sizes. It creates expanded regions around
 * each match by the specified distance thresholds and checks for overlaps. This
 * approach is particularly effective when:
 * <ul>
 *   <li>UI elements have consistent spacing regardless of size</li>
 *   <li>Matches are expected to be within fixed pixel distances</li>
 *   <li>The application has predictable layout margins</li>
 * </ul>
 * 
 * <p><b>Fusion Algorithm:</b>
 * <ol>
 *   <li>Expands each match region by minXDist pixels horizontally and minYDist vertically</li>
 *   <li>Checks if the expanded regions overlap</li>
 *   <li>Fuses matches if any overlap exists</li>
 * </ol>
 * 
 * <p>This strategy is marked as {@code @Primary}, making it the default fusion
 * method when multiple implementations are available. It provides predictable
 * results that work well for most GUI automation scenarios.
 * 
 * @see MatchFusionDecider
 * @see RelativeSizeFusionDecider
 * @see MatchFusion
 */
@Primary
@Component
public class AbsoluteSizeFusionDecider implements MatchFusionDecider {

    /**
     * Determines if two matches should be fused based on absolute distance thresholds.
     * <p>
     * The method expands each match region by the specified distance thresholds in
     * all directions, creating a "fusion zone" around each match. If these expanded
     * regions overlap at any point, the matches are considered close enough to be
     * the same UI element and should be fused.
     * 
     * <p><b>Example:</b> With minXDist=10 and minYDist=10:
     * <ul>
     *   <li>A match at (100,100) size 50x30 creates fusion zone (90,90) to (170,150)</li>
     *   <li>A match at (160,140) size 40x20 creates fusion zone (150,130) to (210,170)</li>
     *   <li>These zones overlap, so the matches would be fused</li>
     * </ul>
     * 
     * @param match1 The first match to evaluate for fusion
     * @param match2 The second match to evaluate for fusion
     * @param minXDist The horizontal expansion distance in pixels. Each match is
     *                 expanded by this amount on both left and right sides.
     * @param minYDist The vertical expansion distance in pixels. Each match is
     *                 expanded by this amount on both top and bottom sides.
     * @return true if the expanded regions overlap (matches should be fused),
     *         false if they don't overlap (matches should remain separate)
     */
    @Override
    public boolean isSameMatchGroup(Match match1, Match match2, int minXDist, int minYDist) {
        Region region1 = new Region(match1.x() - minXDist, match1.y() - minYDist, match1.w() + 2 * minXDist, match1.h() + 2 * minYDist);
        Region region2 = new Region(match2.x() - minXDist, match2.y() - minYDist, match2.w() + 2 * minXDist, match2.h() + 2 * minYDist);
        Optional<Region> overlap = region1.getOverlappingRegion(region2);
        return overlap.isPresent();
    }
}
