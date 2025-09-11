package io.github.jspinak.brobot.analysis.match;

import io.github.jspinak.brobot.model.match.Match;

/**
 * Strategy interface for determining whether two matches should be fused together.
 *
 * <p>Match fusion is a critical process in pattern matching that combines multiple overlapping or
 * closely positioned matches into a single result. This prevents duplicate detections of the same
 * UI element and improves the accuracy of find operations. Different fusion strategies can be
 * implemented to handle various scenarios:
 *
 * <ul>
 *   <li>Overlapping matches from multi-scale searches
 *   <li>Adjacent matches that represent parts of the same element
 *   <li>Matches with slight position variations due to rendering differences
 * </ul>
 *
 * <p>Implementations determine fusion eligibility based on factors such as:
 *
 * <ul>
 *   <li>Spatial proximity (distance between match centers)
 *   <li>Size relationships (relative or absolute)
 *   <li>Match similarity scores
 *   <li>Pattern characteristics
 * </ul>
 *
 * @see MatchFusion
 * @see AbsoluteSizeFusionDecider
 * @see RelativeSizeFusionDecider
 */
public interface MatchFusionDecider {

    /**
     * Determines whether two matches belong to the same logical group and should be fused.
     *
     * <p>This method evaluates whether the provided matches are close enough or similar enough to
     * be considered detections of the same UI element. The decision is based on the
     * implementation's fusion strategy and the specified distance thresholds.
     *
     * <p>Typical fusion criteria include:
     *
     * <ul>
     *   <li>Center-to-center distance less than the specified thresholds
     *   <li>Overlapping regions above a certain percentage
     *   <li>Similar sizes within acceptable tolerances
     *   <li>Compatible match properties (e.g., from the same pattern)
     * </ul>
     *
     * @param match The first match to compare
     * @param match2 The second match to compare
     * @param minXDist The minimum horizontal distance threshold for considering matches as
     *     separate. Matches closer than this distance in the X-axis may be candidates for fusion.
     * @param minYDist The minimum vertical distance threshold for considering matches as separate.
     *     Matches closer than this distance in the Y-axis may be candidates for fusion.
     * @return true if the matches should be fused into a single result, false if they should remain
     *     separate matches
     */
    boolean isSameMatchGroup(Match match, Match match2, int minXDist, int minYDist);
}
