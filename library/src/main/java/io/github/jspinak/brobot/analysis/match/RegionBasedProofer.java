package io.github.jspinak.brobot.analysis.match;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates matches by ensuring they fall within designated search regions.
 * <p>
 * This implementation of {@link MatchProofer} verifies that found matches are
 * contained entirely within the specified search regions. This is crucial for
 * constraining searches to specific areas of the screen and avoiding false
 * positives from unintended areas.
 * 
 * <p><b>Limitation:</b> Matches that span multiple adjacent regions may be
 * rejected even if they partially overlap valid search areas, as the validation
 * requires complete containment within a single region.</p>
 * 
 * @see MatchProofer
 * @see SearchRegionResolver
 * @see Region
 */
@Component
public class RegionBasedProofer implements MatchProofer {

    private final SearchRegionResolver selectRegions;

    public RegionBasedProofer(SearchRegionResolver selectRegions) {
        this.selectRegions = selectRegions;
    }

    /**
     * Validates if a match falls within the allowed search regions for a pattern.
     * <p>
     * This method retrieves the appropriate search regions based on the action options
     * and pattern configuration, then checks if the match is contained within any of
     * these regions. Since regions are unique, there's no risk of duplicate validation.
     * 
     * @param match The match to validate
     * @param actionOptions Configuration that may specify search regions
     * @param pattern The pattern that may have its own search region constraints
     * @return true if the match is contained within at least one search region,
     *         false otherwise
     */
    public boolean isInSearchRegions(Match match, ActionOptions actionOptions, Pattern pattern) {
        // these are unique regions so there won't be any duplicate matches
        List<Region> regions = selectRegions.getRegions(actionOptions, pattern);
        return isInSearchRegions(match, regions);
    }

    /**
     * Checks if a match is completely contained within any of the provided regions.
     * <p>
     * This method iterates through all search regions and returns true if the match
     * is fully contained within at least one region. Note that matches spanning
     * multiple adjacent regions will be rejected, as the validation requires complete
     * containment within a single region. This is a known limitation that may cause
     * valid matches to be filtered out in edge cases.
     * 
     * @param match The match to validate
     * @param regions The list of search regions to check against
     * @return true if the match is completely contained within at least one region,
     *         false if the match is outside all regions or spans multiple regions
     */
    public boolean isInSearchRegions(Match match, List<Region> regions) {
        for (Region r : regions) {
            if (r.contains(match.getRegion())) return true;
        }
        return false;
    }
}
