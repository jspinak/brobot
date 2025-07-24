package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.analysis.match.MatchProofer;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.testing.mock.action.ExecutionModeController;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds all matches for all patterns within StateImages on the screen.
 * <p>
 * This component implements exhaustive pattern matching that locates every occurrence
 * of each pattern within StateImages. It serves as the foundation for both the ALL
 * and BEST find strategies in the Brobot framework:
 * <ul>
 *   <li>{@link ActionOptions.Find#ALL} - Returns all matches found</li>
 *   <li>{@link ActionOptions.Find#BEST} - Uses all matches to select the highest-scoring one</li>
 * </ul>
 * 
 * <p>The component respects search region constraints and validates matches against
 * defined boundaries. Each match is enriched with metadata including pattern information,
 * anchors, position offsets, and state associations.</p>
 * 
 * @see ActionOptions.Find
 * @see Match
 * @see StateImage
 * @see Pattern
 */
@Component
public class FindAll {
    private final SearchRegionResolver selectRegions;
    private final MatchProofer matchProofer;
    private final ExecutionModeController mockOrLive;

    /**
     * Creates a new FindAll instance with required dependencies.
     * 
     * @param selectRegions Service for determining valid search regions based on action options
     * @param matchProofer Validator for ensuring matches fall within allowed regions
     * @param mockOrLive Service for executing pattern matching in mock or live mode
     */
    public FindAll(SearchRegionResolver selectRegions, MatchProofer matchProofer, ExecutionModeController mockOrLive) {
        this.selectRegions = selectRegions;
        this.matchProofer = matchProofer;
        this.mockOrLive = mockOrLive;
    }

    /**
     * Finds all occurrences of patterns from a StateImage within the specified scene.
     * <p>
     * This method performs exhaustive pattern matching, searching for every instance of
     * each pattern contained in the StateImage. The find strategies (FIRST, BEST, EACH, ALL)
     * apply at the StateImage level rather than individual patterns, so all pattern matches
     * are returned regardless of the strategy.
     * 
     * <p>Each match is enriched with metadata including:
     * <ul>
     *   <li>Pattern name and index for identification</li>
     *   <li>Target position and offset from the pattern or action options</li>
     *   <li>Search image data and anchors</li>
     *   <li>State object associations</li>
     *   <li>Scene context information</li>
     * </ul>
     * 
     * <p><b>Implementation Note:</b> When capturing snapshots during execution, they should
     * be added before region-specific filtering occurs. This ensures snapshots remain
     * universally applicable rather than being constrained to specific regions.</p>
     * 
     * @param stateImage The StateImage containing patterns to search for. Must not be null.
     * @param scene The scene (screenshot) to search within. Must not be null.
     * @param actionOptions Configuration controlling search behavior, including target
     *                      position/offset overrides and region constraints.
     * @return A list of all Match objects found for all patterns. Returns an empty list
     *         if no matches are found or all matches are filtered out by region constraints.
     */
    public List<Match> find(StateImage stateImage, Scene scene, ActionOptions actionOptions) {
        List<Match> allMatchObjects = new ArrayList<>();
        for (Pattern pattern : stateImage.getPatterns()) {
            Position targetPosition = actionOptions.getTargetPosition() == null?
                    pattern.getTargetPosition() : actionOptions.getTargetPosition();
            Location xyOffset = actionOptions.getTargetOffset() == null?
                    pattern.getTargetOffset() : actionOptions.getTargetOffset();
            List<Match> matchList = mockOrLive.findAll(pattern, scene);
            addMatchObjects(allMatchObjects, matchList, pattern, stateImage, actionOptions, scene, targetPosition, xyOffset);
        }
        return allMatchObjects;
    }
    
    /**
     * Finds all occurrences of patterns from a StateImage within the specified scene.
     * <p>
     * This method performs exhaustive pattern matching using the new PatternFindOptions API.
     * 
     * @param stateImage The StateImage containing patterns to search for. Must not be null.
     * @param scene The scene (screenshot) to search within. Must not be null.
     * @param findOptions Configuration controlling search behavior.
     * @return A list of all Match objects found for all patterns. Returns an empty list
     *         if no matches are found or all matches are filtered out by region constraints.
     */
    public List<Match> find(StateImage stateImage, Scene scene, PatternFindOptions findOptions) {
        List<Match> allMatchObjects = new ArrayList<>();
        for (Pattern pattern : stateImage.getPatterns()) {
            Position targetPosition = pattern.getTargetPosition();
            Location xyOffset = pattern.getTargetOffset();
            List<Match> matchList = mockOrLive.findAll(pattern, scene);
            addMatchObjects(allMatchObjects, matchList, pattern, stateImage, findOptions, scene, targetPosition, xyOffset);
        }
        return allMatchObjects;
    }

    /**
     * Processes and filters raw matches, converting them to enriched Match objects.
     * <p>
     * This method takes raw matches from pattern detection and transforms them into
     * fully-configured Match objects. Each match is validated against allowed search
     * regions and enriched with comprehensive metadata. Matches outside the allowed
     * regions are filtered out.
     * 
     * @param allMatchObjects The list to which valid matches will be added. Modified by this method.
     * @param matchList Raw matches from the pattern detection engine
     * @param pattern The pattern that was searched for
     * @param stateImage The StateImage containing the pattern
     * @param actionOptions Configuration controlling search behavior and region constraints
     * @param scene The scene where matches were found
     * @param target The target position to apply to matches
     * @param offset The offset to apply to match locations
     */
    private void addMatchObjects(List<Match> allMatchObjects, List<Match> matchList, Pattern pattern,
                               StateImage stateImage, ActionOptions actionOptions, Scene scene,
                               Position target, Location offset) {
        int i=0;
        String name = pattern.getName() != null && !pattern.getName().isEmpty() ?
                pattern.getName() : scene.getPattern().getName();
        for (Match match : matchList) {
            List<Region> regionsAllowedForMatch = selectRegions.getRegions(actionOptions, stateImage);
            if (matchProofer.isInSearchRegions(match, regionsAllowedForMatch)) {
                Match newMatch = new Match.Builder()
                        .setMatch(match)
                        .setName(name+"-"+i)
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
    }
    
    /**
     * Processes and filters raw matches for PatternFindOptions.
     */
    private void addMatchObjects(List<Match> allMatchObjects, List<Match> matchList, Pattern pattern,
                               StateImage stateImage, PatternFindOptions findOptions, Scene scene,
                               Position target, Location offset) {
        int i=0;
        String name = pattern.getName() != null && !pattern.getName().isEmpty() ?
                pattern.getName() : scene.getPattern().getName();
        for (Match match : matchList) {
            List<Region> regionsAllowedForMatch = selectRegions.getRegions(findOptions, stateImage);
            if (matchProofer.isInSearchRegions(match, regionsAllowedForMatch)) {
                Match newMatch = new Match.Builder()
                        .setMatch(match)
                        .setName(name+"-"+i)
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
    }

    /**
     * Finds all text words within the specified scene, filtered by search regions.
     * <p>
     * This method performs OCR (Optical Character Recognition) on the scene to detect
     * all text elements, then filters the results to include only words that fall
     * within the allowed search regions defined by the action options.
     * 
     * @param scene The scene to search for text within. Must not be null.
     * @param actionOptions Configuration defining the search regions for filtering results.
     *                      If no regions are specified, the entire scene is searched.
     * @return A list of Match objects representing detected words within the allowed regions.
     *         Each match contains the word's location and extracted text. Returns an empty
     *         list if no words are found or all words are outside the search regions.
     */
    public List<Match> findWords(Scene scene, ActionOptions actionOptions) {
        List<Match> wordMatches = mockOrLive.findAllWords(scene);
        List<Region> regions = selectRegions.getRegions(actionOptions);
        List<Match> matchesInRegion = new ArrayList<>();
        for (Match match : wordMatches) {
            if (matchProofer.isInSearchRegions(match, regions)) {
                matchesInRegion.add(match);
            }
        }
        return matchesInRegion;
    }

}
