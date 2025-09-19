package io.github.jspinak.brobot.analysis.match;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.util.coordinates.CoordinateScaler;

/**
 * Validates matches by ensuring they fall within designated search regions.
 *
 * <p>This implementation of {@link MatchProofer} verifies that found matches are contained entirely
 * within the specified search regions. This is crucial for constraining searches to specific areas
 * of the screen and avoiding false positives from unintended areas.
 *
 * <p><b>Limitation:</b> Matches that span multiple adjacent regions may be rejected even if they
 * partially overlap valid search areas, as the validation requires complete containment within a
 * single region.
 *
 * @see MatchProofer
 * @see SearchRegionResolver
 * @see Region
 */
@Component
public class RegionBasedProofer implements MatchProofer {

    private final SearchRegionResolver selectRegions;
    private final CoordinateScaler coordinateScaler;

    @Autowired
    public RegionBasedProofer(
            SearchRegionResolver selectRegions,
            @Autowired(required = false) CoordinateScaler coordinateScaler) {
        this.selectRegions = selectRegions;
        this.coordinateScaler = coordinateScaler;
    }

    /**
     * Validates if a match falls within the allowed search regions for a pattern.
     *
     * <p>This method retrieves the appropriate search regions based on the action options and
     * pattern configuration, then checks if the match is contained within any of these regions.
     * Since regions are unique, there's no risk of duplicate validation.
     *
     * <p>When coordinate scaling is active (e.g., with DPI scaling), this method handles the
     * conversion between physical coordinates (from FFmpeg capture) and logical coordinates (for
     * search regions) to ensure accurate containment checks.
     *
     * @param match The match to validate (in physical coordinates from FFmpeg)
     * @param actionConfig Configuration that may specify search regions
     * @param pattern The pattern that may have its own search region constraints
     * @return true if the match is contained within at least one search region, false otherwise
     */
    public boolean isInSearchRegions(Match match, ActionConfig actionConfig, Pattern pattern) {
        // these are unique regions so there won't be any duplicate matches
        List<Region> regions = selectRegions.getRegions(actionConfig, pattern);
        return isInSearchRegions(match, regions);
    }

    /**
     * Checks if a match is completely contained within any of the provided regions.
     *
     * <p>This method iterates through all search regions and returns true if the match is fully
     * contained within at least one region. Note that matches spanning multiple adjacent regions
     * will be rejected, as the validation requires complete containment within a single region.
     * This is a known limitation that may cause valid matches to be filtered out in edge cases.
     *
     * <p>When coordinate scaling is active (e.g., with DPI scaling), this method scales the match
     * region from physical coordinates (FFmpeg capture resolution) to logical coordinates (display
     * resolution) before performing containment checks. This ensures accurate validation when
     * captures are done at a different resolution than the display.
     *
     * @param match The match to validate (in physical coordinates from FFmpeg)
     * @param regions The list of search regions to check against (in logical coordinates)
     * @return true if the match is completely contained within at least one region, false if the
     *     match is outside all regions or spans multiple regions
     */
    public boolean isInSearchRegions(Match match, List<Region> regions) {
        // Get the match region, potentially scaling it from physical to logical coordinates
        Region matchRegion = match.getRegion();

        // If coordinate scaling is needed, scale the match region from physical to logical
        // This is necessary when FFmpeg captures at physical resolution (e.g., 1920x1080)
        // but search regions are defined in logical resolution (e.g., 1536x864 with 125% DPI)
        if (coordinateScaler != null && coordinateScaler.isScalingNeeded()) {
            matchRegion = coordinateScaler.scaleRegionToLogical(matchRegion);
        }

        // Check if the (potentially scaled) match region is contained in any search region
        for (Region r : regions) {
            if (r.contains(matchRegion)) return true;
        }
        return false;
    }
}
