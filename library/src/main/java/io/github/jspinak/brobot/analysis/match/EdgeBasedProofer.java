package io.github.jspinak.brobot.analysis.match;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Edge-based validation strategy for determining if matches fall within search regions.
 * <p>
 * This implementation validates matches by checking if all four corner points
 * (edges) of the match rectangle fall within the designated search regions. This
 * approach is more permissive than strict containment, allowing matches that span
 * multiple adjacent regions to be accepted.
 * 
 * <p><b>Advantages:</b>
 * <ul>
 *   <li>Accepts matches that span multiple adjacent regions</li>
 *   <li>Works well with tiled or grid-based search areas</li>
 *   <li>Handles matches at region boundaries gracefully</li>
 * </ul>
 * 
 * <p><b>Limitations:</b>
 * <ul>
 *   <li>May accept matches in gaps between non-adjacent regions if the
 *       corners happen to fall in valid regions</li>
 *   <li>A match with corners in different regions but center in empty space
 *       would still be accepted</li>
 * </ul>
 * 
 * <p>This is marked as {@code @Primary}, making it the default validation
 * strategy when multiple implementations are available.
 * 
 * @see MatchProofer
 * @see RegionBasedProofer
 * @see SearchRegionResolver
 */
@Primary
@Component
public class EdgeBasedProofer implements MatchProofer {

    private final SearchRegionResolver selectRegions;

    /**
     * Creates a new edge-based match proofer.
     * 
     * @param selectRegions Service for resolving search regions from various sources
     */
    public EdgeBasedProofer(SearchRegionResolver selectRegions) {
        this.selectRegions = selectRegions;
    }

    /**
     * Validates if a match is within regions derived from action options and pattern.
     * <p>
     * This method first resolves the appropriate search regions using the
     * {@link SearchRegionResolver} service, which handles the priority hierarchy between
     * pattern-defined regions and action option overrides. It then delegates to
     * the edge-based validation logic.
     * 
     * @param match The match to validate
     * @param actionOptions Configuration that may override pattern regions
     * @param pattern The pattern that may define its own search regions
     * @return true if all four corners of the match fall within the resolved
     *         search regions, false otherwise
     */
    @Override
    public boolean isInSearchRegions(Match match, ActionOptions actionOptions, Pattern pattern) {
        // these are unique regions so there won't be any duplicate matches
        List<Region> regions = selectRegions.getRegions(actionOptions, pattern);
        return isInSearchRegions(match, regions);
    }

    /**
     * Validates if all four corners of a match fall within the search regions.
     * <p>
     * This method implements an edge-based validation strategy that checks each
     * corner point of the match rectangle. A match is considered valid if all
     * four corners (top-left, top-right, bottom-left, bottom-right) fall within
     * at least one of the provided search regions.
     * 
     * <p><b>Algorithm:</b>
     * <ol>
     *   <li>Calculate the four corner coordinates of the match rectangle</li>
     *   <li>For each search region, check which corners it contains</li>
     *   <li>Track which corners have been validated</li>
     *   <li>Return true only if all four corners are within regions</li>
     * </ol>
     * 
     * <p><b>Example scenarios:</b>
     * <ul>
     *   <li>Match entirely within one region: All corners in same region ✓</li>
     *   <li>Match spanning two adjacent regions: Corners split between regions ✓</li>
     *   <li>Match in gap with corners in different regions: All corners validated ✓</li>
     *   <li>Match partially outside all regions: At least one corner invalid ✗</li>
     * </ul>
     * 
     * @param match The match to validate by its corner positions
     * @param regions The list of valid search regions. Corners must fall within
     *                these regions for the match to be accepted.
     * @return true if all four corners of the match are within search regions,
     *         false if any corner falls outside all regions
     */
    @Override
    public boolean isInSearchRegions(Match match, List<Region> regions) {
        boolean topLeft = false, topRight = false, bottomLeft = false, bottomRight = false;
        Location tL = new Location(match.x(), match.y());
        Location tR = new Location(match.x()+match.w(), match.y());
        Location bL = new Location(match.x(), match.y() + match.h());
        Location bR = new Location(match.x()+match.w(), match.y()+match.h());
        for (Region region : regions) {
            if (region.contains(tL.sikuli())) topLeft = true;
            if (region.contains(tR.sikuli())) topRight = true;
            if (region.contains(bL.sikuli())) bottomLeft = true;
            if (region.contains(bR.sikuli())) bottomRight = true;
            if (topLeft && topRight && bottomLeft && bottomRight) return true;
        }
        return false;
    }
}
