package io.github.jspinak.brobot.tools.testing.data;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.match.Match;

/**
 * Utility class for creating ActionRecord instances for testing and mock data initialization.
 * 
 * <p>This class provides convenience methods for creating ActionRecord instances with
 * common configurations, making it easier to set up test data and initialize states
 * with mock action history.</p>
 * 
 * <p>Example usage in state initialization:
 * <pre>{@code
 * public class HomeState {
 *     private final StateImage button;
 *     
 *     public HomeState() {
 *         button = new StateImage.Builder()
 *             .addPatterns("button.png")
 *             .build();
 *             
 *         // Add mock action history for testing
 *         button.getPatterns().get(0).getActionHistory()
 *             .addSnapshot(ActionRecordTestUtils.createActionRecord(
 *                 0.95, 220, 600, 20, 20));
 *     }
 * }
 * }</pre>
 * 
 * @since 1.1.0
 */
public class ActionRecordTestUtils {
    
    /**
     * Creates an ActionRecord with a successful find operation at the specified location.
     * Uses default PatternFindOptions with BEST strategy.
     * 
     * @param similarity the similarity score of the match (0.0 to 1.0)
     * @param x the x-coordinate of the match
     * @param y the y-coordinate of the match
     * @param w the width of the match region
     * @param h the height of the match region
     * @return a new ActionRecord configured for testing
     */
    public static ActionRecord createActionRecord(double similarity, int x, int y, int w, int h) {
        return new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(similarity)
                .build())
            .addMatch(new Match.Builder()
                .setRegion(x, y, w, h)
                .setSimScore(similarity)
                .build())
            .setActionSuccess(true)
            .build();
    }
    
    /**
     * Creates an ActionRecord with a successful find operation at the specified location
     * using custom ActionConfig.
     * 
     * @param actionConfig the ActionConfig to use for this record
     * @param similarity the similarity score of the match (0.0 to 1.0)
     * @param x the x-coordinate of the match
     * @param y the y-coordinate of the match
     * @param w the width of the match region
     * @param h the height of the match region
     * @return a new ActionRecord configured for testing
     */
    public static ActionRecord createActionRecord(ActionConfig actionConfig, 
                                                  double similarity, 
                                                  int x, int y, int w, int h) {
        return new ActionRecord.Builder()
            .setActionConfig(actionConfig)
            .addMatch(new Match.Builder()
                .setRegion(x, y, w, h)
                .setSimScore(similarity)
                .build())
            .setActionSuccess(true)
            .build();
    }
    
    /**
     * Creates a successful ActionRecord with the specified configuration and match.
     * 
     * @param actionConfig the ActionConfig for this record
     * @param match the Match object representing the found element
     * @return a new ActionRecord configured for testing
     */
    public static ActionRecord createSuccessRecord(ActionConfig actionConfig, Match match) {
        return new ActionRecord.Builder()
            .setActionConfig(actionConfig)
            .addMatch(match)
            .setActionSuccess(true)
            .setResultSuccess(true)
            .build();
    }
    
    /**
     * Creates a successful ActionRecord with default find configuration.
     * 
     * @param similarity the similarity score of the match
     * @param x the x-coordinate of the match
     * @param y the y-coordinate of the match
     * @return a new ActionRecord with default dimensions (80x30)
     */
    public static ActionRecord createSuccessRecord(double similarity, int x, int y) {
        return createActionRecord(similarity, x, y, 80, 30);
    }
    
    /**
     * Creates a failed ActionRecord (no matches found).
     * 
     * @return a new ActionRecord representing a failed find operation
     */
    public static ActionRecord createFailureRecord() {
        return new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.85)
                .build())
            .setActionSuccess(false)
            .setResultSuccess(false)
            .build();
    }
    
    /**
     * Creates a failed ActionRecord with specific ActionConfig.
     * 
     * @param actionConfig the ActionConfig that resulted in failure
     * @return a new ActionRecord representing a failed operation
     */
    public static ActionRecord createFailureRecord(ActionConfig actionConfig) {
        return new ActionRecord.Builder()
            .setActionConfig(actionConfig)
            .setActionSuccess(false)
            .setResultSuccess(false)
            .build();
    }
    
    /**
     * Creates an ActionRecord with multiple matches (for Find.ALL scenarios).
     * 
     * @param similarity the similarity score for all matches
     * @param matches array of match coordinates [x, y, w, h] for each match
     * @return a new ActionRecord with multiple matches
     */
    public static ActionRecord createMultiMatchRecord(double similarity, int[]... matches) {
        ActionRecord.Builder builder = new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(similarity)
                .build())
            .setActionSuccess(true);
            
        for (int[] match : matches) {
            if (match.length >= 4) {
                builder.addMatch(new Match.Builder()
                    .setRegion(match[0], match[1], match[2], match[3])
                    .setSimScore(similarity)
                    .build());
            }
        }
        
        return builder.build();
    }
    
    /**
     * Creates an ActionRecord with text extraction results.
     * 
     * @param text the extracted text
     * @param x the x-coordinate where text was found
     * @param y the y-coordinate where text was found
     * @param w the width of the text region
     * @param h the height of the text region
     * @return a new ActionRecord with text results
     */
    public static ActionRecord createTextRecord(String text, int x, int y, int w, int h) {
        return new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build())
            .setText(text)
            .addMatch(new Match.Builder()
                .setRegion(x, y, w, h)
                .setText(text)
                .build())
            .setActionSuccess(true)
            .build();
    }
    
    /**
     * Creates an ActionRecord with a specific duration (for performance testing).
     * 
     * @param similarity the similarity score of the match
     * @param x the x-coordinate of the match
     * @param y the y-coordinate of the match
     * @param w the width of the match region
     * @param h the height of the match region
     * @param durationSeconds the duration of the operation in seconds
     * @return a new ActionRecord with timing information
     */
    public static ActionRecord createTimedRecord(double similarity, 
                                                 int x, int y, int w, int h, 
                                                 double durationSeconds) {
        return new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(similarity)
                .build())
            .addMatch(new Match.Builder()
                .setRegion(x, y, w, h)
                .setSimScore(similarity)
                .build())
            .setDuration(durationSeconds)
            .setActionSuccess(true)
            .build();
    }
}