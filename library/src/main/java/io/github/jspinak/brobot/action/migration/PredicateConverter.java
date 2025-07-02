package io.github.jspinak.brobot.action.migration;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;

import java.util.function.Predicate;
import java.util.List;

/**
 * Utility class to help with predicate-based success criteria during migration.
 * <p>
 * Since ActionOptions used Predicate on a Matches object that no longer exists,
 * and the new ActionConfig uses Predicate&lt;ActionResult&gt;, this converter provides
 * helper methods to create common success criteria predicates.
 * </p>
 * <p>
 * This is a temporary utility that will be removed once all code has been
 * migrated to use ActionResult predicates directly.
 * </p>
 * 
 * @since 1.0
 */
public class PredicateConverter {
    
    /**
     * Creates a default success predicate for ActionResult.
     * <p>
     * This predicate considers the action successful if:
     * <ul>
     *   <li>The success flag is true</li>
     *   <li>OR at least one match was found</li>
     * </ul>
     * </p>
     * 
     * @return A predicate that checks for basic success conditions
     */
    public static Predicate<ActionResult> defaultSuccessCriteria() {
        return actionResult -> actionResult.isSuccess() || !actionResult.getMatchList().isEmpty();
    }
    
    /**
     * Creates a predicate that requires a minimum number of matches.
     * 
     * @param minMatches The minimum number of matches required
     * @return A predicate that checks if enough matches were found
     */
    public static Predicate<ActionResult> requireMinMatches(int minMatches) {
        return actionResult -> actionResult.getMatchList().size() >= minMatches;
    }
    
    /**
     * Creates a predicate that requires all matches to have a minimum similarity score.
     * 
     * @param minSimilarity The minimum similarity score (0.0 to 1.0)
     * @return A predicate that checks if all matches meet the similarity threshold
     */
    public static Predicate<ActionResult> requireMinSimilarity(double minSimilarity) {
        return actionResult -> {
            List<Match> matches = actionResult.getMatchList();
            if (matches.isEmpty()) {
                return false;
            }
            return matches.stream().allMatch(match -> match.getScore() >= minSimilarity);
        };
    }
    
    /**
     * Creates a predicate that requires the action to be successful AND have matches.
     * 
     * @return A predicate that checks both success flag and match presence
     */
    public static Predicate<ActionResult> requireSuccessAndMatches() {
        return actionResult -> actionResult.isSuccess() && !actionResult.getMatchList().isEmpty();
    }
    
    /**
     * Creates a predicate that requires matches in a specific region.
     * 
     * @param x The x-coordinate of the region
     * @param y The y-coordinate of the region
     * @param width The width of the region
     * @param height The height of the region
     * @return A predicate that checks if any match is within the specified region
     */
    public static Predicate<ActionResult> requireMatchInRegion(int x, int y, int width, int height) {
        return actionResult -> {
            return actionResult.getMatchList().stream()
                .anyMatch(match -> {
                    var region = match.getRegion();
                    return region.getX() >= x && 
                           region.getY() >= y &&
                           region.getX() + region.getW() <= x + width &&
                           region.getY() + region.getH() <= y + height;
                });
        };
    }
    
    /**
     * Combines multiple predicates with AND logic.
     * 
     * @param predicates The predicates to combine
     * @return A predicate that returns true only if all predicates are true
     */
    @SafeVarargs
    public static Predicate<ActionResult> allOf(Predicate<ActionResult>... predicates) {
        return actionResult -> {
            for (Predicate<ActionResult> predicate : predicates) {
                if (!predicate.test(actionResult)) {
                    return false;
                }
            }
            return true;
        };
    }
    
    /**
     * Combines multiple predicates with OR logic.
     * 
     * @param predicates The predicates to combine
     * @return A predicate that returns true if any predicate is true
     */
    @SafeVarargs
    public static Predicate<ActionResult> anyOf(Predicate<ActionResult>... predicates) {
        return actionResult -> {
            for (Predicate<ActionResult> predicate : predicates) {
                if (predicate.test(actionResult)) {
                    return true;
                }
            }
            return false;
        };
    }
}