package io.github.jspinak.brobot.action.basic.find;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.analysis.match.MatchProofer;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig.VerbosityLevel;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.testing.mock.action.ExecutionModeController;

import lombok.extern.slf4j.Slf4j;

/**
 * Finds all matches for all patterns within StateImages on the screen.
 *
 * <p>This component implements exhaustive pattern matching that locates every occurrence of each
 * pattern within StateImages. It serves as the foundation for both the ALL and BEST find strategies
 * in the Brobot framework:
 *
 * <ul>
 *   <li>ALL - Returns all matches found
 *   <li>BEST - Uses all matches to select the highest-scoring one
 * </ul>
 *
 * <p>The component respects search region constraints and validates matches against defined
 * boundaries. Each match is enriched with metadata including pattern information, anchors, position
 * offsets, and state associations.
 *
 * @see PatternFindOptions.Strategy
 * @see Match
 * @see StateImage
 * @see Pattern
 */
@Slf4j
@Component
public class FindAll {
    private final SearchRegionResolver selectRegions;
    private final MatchProofer matchProofer;
    private final ExecutionModeController mockOrLive;

    @Autowired(required = false)
    private LoggingVerbosityConfig verbosityConfig;

    /**
     * Creates a new FindAll instance with required dependencies.
     *
     * @param selectRegions Service for determining valid search regions based on action options
     * @param matchProofer Validator for ensuring matches fall within allowed regions
     * @param mockOrLive Service for executing pattern matching in mock or live mode
     */
    public FindAll(
            SearchRegionResolver selectRegions,
            MatchProofer matchProofer,
            ExecutionModeController mockOrLive) {
        this.selectRegions = selectRegions;
        this.matchProofer = matchProofer;
        this.mockOrLive = mockOrLive;
    }

    /**
     * Finds all occurrences of patterns from a StateImage within the specified scene.
     *
     * <p>This method performs exhaustive pattern matching, searching for every instance of each
     * pattern contained in the StateImage. The find strategies (FIRST, BEST, EACH, ALL) apply at
     * the StateImage level rather than individual patterns, so all pattern matches are returned
     * regardless of the strategy.
     *
     * <p>Each match is enriched with metadata including:
     *
     * <ul>
     *   <li>Pattern name and index for identification
     *   <li>Target position and offset from the pattern or action options
     *   <li>Search image data and anchors
     *   <li>State object associations
     *   <li>Scene context information
     * </ul>
     *
     * <p><b>Implementation Note:</b> When capturing snapshots during execution, they should be
     * added before region-specific filtering occurs. This ensures snapshots remain universally
     * applicable rather than being constrained to specific regions.
     *
     * @param stateImage The StateImage containing patterns to search for. Must not be null.
     * @param scene The scene (screenshot) to search within. Must not be null.
     * @param actionConfig Configuration controlling search behavior, including target
     *     position/offset overrides and region constraints.
     * @return A list of all Match objects found for all patterns. Returns an empty list if no
     *     matches are found or all matches are filtered out by region constraints.
     */
    public List<Match> find(StateImage stateImage, Scene scene, ActionConfig actionConfig) {
        log.info("[FINDALL_DEBUG] Finding patterns for StateImage: {}", stateImage.getName());
        log.info(
                "[FINDALL_DEBUG]   - Has SearchRegionOnObject: {}",
                stateImage.getSearchRegionOnObject() != null);
        if (stateImage.getSearchRegionOnObject() != null) {
            log.info(
                    "[FINDALL_DEBUG]   - SearchRegionOnObject target: {}.{}",
                    stateImage.getSearchRegionOnObject().getTargetStateName(),
                    stateImage.getSearchRegionOnObject().getTargetObjectName());
        }
        log.info("[FINDALL_DEBUG]   - Number of patterns: {}", stateImage.getPatterns().size());

        List<Match> allMatchObjects = new ArrayList<>();
        int patternIndex = 0;
        for (Pattern pattern : stateImage.getPatterns()) {
            log.info(
                    "[FINDALL_DEBUG]   Processing pattern {} (index {})",
                    pattern.getNameWithoutExtension(),
                    patternIndex);
            log.info(
                    "[FINDALL_DEBUG]     - Pattern has search regions: {}",
                    pattern.getSearchRegions() != null
                            && !pattern.getSearchRegions().getAllRegions().isEmpty());
            if (pattern.getSearchRegions() != null
                    && !pattern.getSearchRegions().getAllRegions().isEmpty()) {
                log.info(
                        "[FINDALL_DEBUG]     - Pattern search regions count: {}",
                        pattern.getSearchRegions().getAllRegions().size());
                for (Region r : pattern.getSearchRegions().getAllRegions()) {
                    log.info("[FINDALL_DEBUG]       - Region: {}", r);
                }
            }

            // Use pattern's target position and offset
            Position targetPosition = pattern.getTargetPosition();
            Location xyOffset = pattern.getTargetOffset();

            // CRITICAL: The pattern being passed to mockOrLive.findAll does NOT have the
            // SearchRegionOnObject from its parent StateImage!
            // The pattern's search regions should have been set by DynamicRegionResolver
            // BEFORE we get here.

            List<Match> matchList = mockOrLive.findAll(pattern, scene);
            log.info(
                    "[FINDALL_DEBUG]     - Found {} matches for pattern {}",
                    matchList.size(),
                    pattern.getNameWithoutExtension());

            addMatchObjects(
                    allMatchObjects,
                    matchList,
                    pattern,
                    stateImage,
                    actionConfig,
                    scene,
                    targetPosition,
                    xyOffset);
            patternIndex++;
        }

        log.info(
                "[FINDALL_DEBUG] Total matches found for {}: {}",
                stateImage.getName(),
                allMatchObjects.size());
        return allMatchObjects;
    }

    /**
     * Finds all occurrences of patterns from a StateImage within the specified scene.
     *
     * <p>This method performs exhaustive pattern matching using the new PatternFindOptions API.
     *
     * @param stateImage The StateImage containing patterns to search for. Must not be null.
     * @param scene The scene (screenshot) to search within. Must not be null.
     * @param findOptions Configuration controlling search behavior.
     * @return A list of all Match objects found for all patterns. Returns an empty list if no
     *     matches are found or all matches are filtered out by region constraints.
     */
    public List<Match> find(StateImage stateImage, Scene scene, PatternFindOptions findOptions) {
        log.info(
                "[FINDALL_DEBUG] Finding patterns for StateImage: {} (PatternFindOptions version)",
                stateImage.getName());
        log.info(
                "[FINDALL_DEBUG]   - Has SearchRegionOnObject: {}",
                stateImage.getSearchRegionOnObject() != null);
        if (stateImage.getSearchRegionOnObject() != null) {
            log.info(
                    "[FINDALL_DEBUG]   - SearchRegionOnObject target: {}.{}",
                    stateImage.getSearchRegionOnObject().getTargetStateName(),
                    stateImage.getSearchRegionOnObject().getTargetObjectName());
        }
        log.info("[FINDALL_DEBUG]   - Number of patterns: {}", stateImage.getPatterns().size());

        List<Match> allMatchObjects = new ArrayList<>();
        int patternIndex = 0;
        for (Pattern pattern : stateImage.getPatterns()) {
            log.info(
                    "[FINDALL_DEBUG]   Processing pattern {} (index {})",
                    pattern.getNameWithoutExtension(),
                    patternIndex);
            log.info(
                    "[FINDALL_DEBUG]     - Pattern has search regions: {}",
                    pattern.getSearchRegions() != null
                            && !pattern.getSearchRegions().getAllRegions().isEmpty());
            if (pattern.getSearchRegions() != null
                    && !pattern.getSearchRegions().getAllRegions().isEmpty()) {
                log.info(
                        "[FINDALL_DEBUG]     - Pattern search regions count: {}",
                        pattern.getSearchRegions().getAllRegions().size());
            }

            Position targetPosition = pattern.getTargetPosition();
            Location xyOffset = pattern.getTargetOffset();
            List<Match> matchList = mockOrLive.findAll(pattern, scene);
            log.info(
                    "[FINDALL_DEBUG]     - Found {} matches for pattern {}",
                    matchList.size(),
                    pattern.getNameWithoutExtension());

            addMatchObjects(
                    allMatchObjects,
                    matchList,
                    pattern,
                    stateImage,
                    findOptions,
                    scene,
                    targetPosition,
                    xyOffset);
            patternIndex++;
        }

        log.info(
                "[FINDALL_DEBUG] Total matches found for {}: {}",
                stateImage.getName(),
                allMatchObjects.size());
        return allMatchObjects;
    }

    /**
     * Processes and filters raw matches, converting them to enriched Match objects.
     *
     * <p>This method takes raw matches from pattern detection and transforms them into
     * fully-configured Match objects. Each match is validated against allowed search regions and
     * enriched with comprehensive metadata. Matches outside the allowed regions are filtered out.
     *
     * @param allMatchObjects The list to which valid matches will be added. Modified by this
     *     method.
     * @param matchList Raw matches from the pattern detection engine
     * @param pattern The pattern that was searched for
     * @param stateImage The StateImage containing the pattern
     * @param actionConfig Configuration controlling search behavior and region constraints
     * @param scene The scene where matches were found
     * @param target The target position to apply to matches
     * @param offset The offset to apply to match locations
     */
    private void addMatchObjects(
            List<Match> allMatchObjects,
            List<Match> matchList,
            Pattern pattern,
            StateImage stateImage,
            ActionConfig actionConfig,
            Scene scene,
            Position target,
            Location offset) {
        int i = 0;
        String name =
                pattern.getNameWithoutExtension() != null
                                && !pattern.getNameWithoutExtension().isEmpty()
                        ? pattern.getNameWithoutExtension()
                        : scene.getPattern().getNameWithoutExtension();
        List<Region> regionsAllowedForMatch = selectRegions.getRegions(actionConfig, stateImage);

        // Compact debug logging - only log if there are matches or in verbose mode
        boolean hasMatches = !matchList.isEmpty();
        boolean isVerbose =
                verbosityConfig != null && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE;

        if (hasMatches || isVerbose) {
            // Deduplicate regions for cleaner output
            Set<String> uniqueRegions = new LinkedHashSet<>();
            for (Region r : regionsAllowedForMatch) {
                uniqueRegions.add(r.toString());
            }

            // Single-line summary
            if (isVerbose || hasMatches) {
                System.out.println(
                        "[FILTER] '"
                                + name
                                + "': "
                                + matchList.size()
                                + " matches → "
                                + uniqueRegions.size()
                                + " region(s) "
                                + (uniqueRegions.size() > 0
                                        ? uniqueRegions.iterator().next()
                                        : "[]"));
            }
        }

        for (Match match : matchList) {
            boolean inRegion = matchProofer.isInSearchRegions(match, regionsAllowedForMatch);
            // Only log mismatches in verbose mode
            if (!inRegion && isVerbose) {
                System.out.println(
                        "  [FILTER] Excluded match at "
                                + match.getRegion()
                                + " (outside search region)");
            }

            if (inRegion) {
                Match newMatch =
                        new Match.Builder()
                                .setMatch(match)
                                .setName(name + "-" + i)
                                .setPosition(target)
                                .setOffset(offset)
                                .setSearchImage(pattern.getBImage())
                                .setAnchors(pattern.getAnchors())
                                .setStateObjectData(stateImage)
                                .setScene(scene)
                                .build();
                allMatchObjects.add(newMatch);
                i++;
            }
        }

        // Only log final count if matches were filtered
        if (isVerbose && i != matchList.size()) {
            System.out.println("  [FILTER] After filtering: " + i + " matches remain");
        }

        // Set fixed region for the pattern if it's marked as fixed and we have valid matches
        if (pattern.isFixed() && !allMatchObjects.isEmpty()) {
            // Find the best match among the filtered matches
            Match bestMatch = null;
            double bestScore = 0;
            for (Match match : allMatchObjects) {
                if (match.getScore() > bestScore) {
                    bestScore = match.getScore();
                    bestMatch = match;
                }
            }

            // Get the minimum similarity threshold from actionConfig or use default
            double minSimilarity = 0.7; // default
            if (actionConfig instanceof BaseFindOptions) {
                minSimilarity = ((BaseFindOptions) actionConfig).getSimilarity();
            }

            if (bestMatch != null && bestScore >= minSimilarity) {
                if (isVerbose) {
                    System.out.println(
                            "[FIXED] Pattern '"
                                    + name
                                    + "' → "
                                    + bestMatch.getRegion()
                                    + " (score: "
                                    + String.format("%.3f", bestScore)
                                    + ")");
                }
                pattern.getSearchRegions().setFixedRegion(bestMatch.getRegion());
            } else {
                System.out.println(
                        "[FIXED REGION] Not setting fixed region - best score "
                                + String.format("%.3f", bestScore)
                                + " below threshold "
                                + String.format("%.3f", minSimilarity));
            }
        }
    }

    /** Processes and filters raw matches for PatternFindOptions. */
    private void addMatchObjects(
            List<Match> allMatchObjects,
            List<Match> matchList,
            Pattern pattern,
            StateImage stateImage,
            PatternFindOptions findOptions,
            Scene scene,
            Position target,
            Location offset) {
        int i = 0;
        String name =
                pattern.getNameWithoutExtension() != null
                                && !pattern.getNameWithoutExtension().isEmpty()
                        ? pattern.getNameWithoutExtension()
                        : scene.getPattern().getNameWithoutExtension();
        List<Match> validMatches = new ArrayList<>();
        boolean isVerbose =
                verbosityConfig != null && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE;

        for (Match match : matchList) {
            List<Region> regionsAllowedForMatch = selectRegions.getRegions(findOptions, stateImage);
            if (matchProofer.isInSearchRegions(match, regionsAllowedForMatch)) {
                Match newMatch =
                        new Match.Builder()
                                .setMatch(match)
                                .setName(name + "-" + i)
                                .setPosition(target)
                                .setOffset(offset)
                                .setSearchImage(pattern.getBImage())
                                .setAnchors(pattern.getAnchors())
                                .setStateObjectData(stateImage)
                                .setScene(scene)
                                .build();
                allMatchObjects.add(newMatch);
                validMatches.add(newMatch);
                i++;
            }
        }

        // Set fixed region for the pattern if it's marked as fixed and we have valid matches
        if (pattern.isFixed() && !validMatches.isEmpty()) {
            // Find the best match among the filtered matches
            Match bestMatch = null;
            double bestScore = 0;
            for (Match match : validMatches) {
                if (match.getScore() > bestScore) {
                    bestScore = match.getScore();
                    bestMatch = match;
                }
            }

            // Use the similarity from findOptions
            double minSimilarity = findOptions.getSimilarity();

            if (bestMatch != null && bestScore >= minSimilarity) {
                if (isVerbose) {
                    System.out.println(
                            "[FIXED] Pattern '"
                                    + name
                                    + "' → "
                                    + bestMatch.getRegion()
                                    + " (score: "
                                    + String.format("%.3f", bestScore)
                                    + ")");
                }
                pattern.getSearchRegions().setFixedRegion(bestMatch.getRegion());
            }
        }
    }

    /**
     * Finds all text words within the specified scene, filtered by search regions.
     *
     * <p>This method performs OCR (Optical Character Recognition) on the scene to detect all text
     * elements, then filters the results to include only words that fall within the allowed search
     * regions defined by the action options.
     *
     * @param scene The scene to search for text within. Must not be null.
     * @param actionConfig Configuration defining the search regions for filtering results. If no
     *     regions are specified, the entire scene is searched.
     * @return A list of Match objects representing detected words within the allowed regions. Each
     *     match contains the word's location and extracted text. Returns an empty list if no words
     *     are found or all words are outside the search regions.
     */
    public List<Match> findWords(Scene scene, ActionConfig actionConfig) {
        List<Match> wordMatches = mockOrLive.findAllWords(scene);
        List<Region> regions = selectRegions.getRegions(actionConfig);
        List<Match> matchesInRegion = new ArrayList<>();
        for (Match match : wordMatches) {
            if (matchProofer.isInSearchRegions(match, regions)) {
                matchesInRegion.add(match);
            }
        }
        return matchesInRegion;
    }
}
