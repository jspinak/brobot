package io.github.jspinak.brobot.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class that scores the logging style similarity for QUIET verbosity mode.
 * Measures the difference between actual logging output and target output.
 */
public class QuietModeLoggingScorerTest {

    private QuietModeLoggingScorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new QuietModeLoggingScorer();
    }

    @Test
    void testPerfectMatchScoring() {
        String target = "✓ Find Working.ClaudeIcon • 234ms";
        String actual = "✓ Find Working.ClaudeIcon • 234ms";
        
        LoggingScore score = scorer.scoreOutput(actual, target);
        
        assertEquals(100.0, score.getTotalScore());
        assertTrue(score.isPerfect());
    }

    @Test
    void testMissingDurationScoring() {
        String target = "✓ Find Working.ClaudeIcon • 234ms";
        String actual = "✓ Find Working.ClaudeIcon";
        
        LoggingScore score = scorer.scoreOutput(actual, target);
        
        assertFalse(score.isPerfect());
        assertEquals(0.0, score.getComponentScores().get("duration"));
    }

    @Test
    void testWrongFormatScoring() {
        String target = "✓ Find Working.ClaudeIcon • 234ms";
        String actual = "▶ Find_COMPLETE:  ✓";
        
        LoggingScore score = scorer.scoreOutput(actual, target);
        
        assertFalse(score.isPerfect());
        assertTrue(score.getTotalScore() < 50.0);
    }

    @Test
    void testMultipleActionsScoring() {
        List<String> targets = List.of(
            "✓ Find Working.ClaudeIcon • 234ms",
            "✗ Find Prompt.ClaudePrompt • 567ms",
            "✓ Click Prompt.SubmitButton • 123ms"
        );
        
        List<String> actuals = List.of(
            "✓ Find Working.ClaudeIcon • 234ms",
            "✗ Find Prompt.ClaudePrompt",  // Missing duration
            "✓ Click Prompt.SubmitButton • 123ms"
        );
        
        MultiLineScore multiScore = scorer.scoreMultipleOutputs(actuals, targets);
        
        assertFalse(multiScore.isPerfect());
        assertEquals(2, multiScore.getPerfectLines());
        assertEquals(1, multiScore.getImperfectLines());
    }
}

/**
 * Helper class that scores the logging style similarity for QUIET verbosity.
 * Measures how closely actual output matches the target format.
 */
class QuietModeLoggingScorer {
    
    // Target format: ✓/✗ Action State.Object • durationms
    private static final Pattern TARGET_PATTERN = Pattern.compile(
        "^([✓✗])\\s+(\\w+)\\s+([\\w.]+)\\s*(?:•\\s*(\\d+)ms)?$"
    );
    
    // Component weights for scoring
    private static final double SYMBOL_WEIGHT = 0.20;
    private static final double ACTION_WEIGHT = 0.20;
    private static final double TARGET_WEIGHT = 0.30;
    private static final double DURATION_WEIGHT = 0.20;
    private static final double FORMAT_WEIGHT = 0.10;
    
    /**
     * Score a single line of output against its target.
     */
    public LoggingScore scoreOutput(String actual, String target) {
        LoggingScore score = new LoggingScore();
        
        // Parse target
        Matcher targetMatcher = TARGET_PATTERN.matcher(target);
        if (!targetMatcher.matches()) {
            score.addError("Target format is invalid: " + target);
            return score;
        }
        
        String targetSymbol = targetMatcher.group(1);
        String targetAction = targetMatcher.group(2);
        String targetObject = targetMatcher.group(3);
        String targetDuration = targetMatcher.group(4);
        
        // Parse actual
        Matcher actualMatcher = TARGET_PATTERN.matcher(actual);
        if (!actualMatcher.matches()) {
            score.addError("Actual output doesn't match expected format: " + actual);
            score.setComponentScore("format", 0.0);
            return score;
        }
        
        score.setComponentScore("format", 100.0);
        
        // Score components
        String actualSymbol = actualMatcher.group(1);
        String actualAction = actualMatcher.group(2);
        String actualObject = actualMatcher.group(3);
        String actualDuration = actualMatcher.group(4);
        
        // Symbol score
        if (targetSymbol.equals(actualSymbol)) {
            score.setComponentScore("symbol", 100.0);
        } else {
            score.setComponentScore("symbol", 0.0);
            score.addError("Symbol mismatch: expected '" + targetSymbol + "' but got '" + actualSymbol + "'");
        }
        
        // Action score
        if (targetAction.equals(actualAction)) {
            score.setComponentScore("action", 100.0);
        } else {
            score.setComponentScore("action", 0.0);
            score.addError("Action mismatch: expected '" + targetAction + "' but got '" + actualAction + "'");
        }
        
        // Target object score
        if (targetObject.equals(actualObject)) {
            score.setComponentScore("target", 100.0);
        } else {
            // Partial credit for partial matches
            double similarity = calculateStringSimilarity(targetObject, actualObject);
            score.setComponentScore("target", similarity * 100);
            if (similarity < 1.0) {
                score.addError("Target mismatch: expected '" + targetObject + "' but got '" + actualObject + "'");
            }
        }
        
        // Duration score
        if (targetDuration != null) {
            if (actualDuration != null && targetDuration.equals(actualDuration)) {
                score.setComponentScore("duration", 100.0);
            } else if (actualDuration != null) {
                // Partial credit if duration exists but differs
                score.setComponentScore("duration", 50.0);
                score.addError("Duration mismatch: expected '" + targetDuration + "ms' but got '" + actualDuration + "ms'");
            } else {
                score.setComponentScore("duration", 0.0);
                score.addError("Missing duration");
            }
        } else if (actualDuration == null) {
            // Both null is okay
            score.setComponentScore("duration", 100.0);
        }
        
        // Calculate total score
        double totalScore = 
            score.getComponentScores().get("symbol") * SYMBOL_WEIGHT +
            score.getComponentScores().get("action") * ACTION_WEIGHT +
            score.getComponentScores().get("target") * TARGET_WEIGHT +
            score.getComponentScores().get("duration") * DURATION_WEIGHT +
            score.getComponentScores().get("format") * FORMAT_WEIGHT;
            
        score.setTotalScore(totalScore);
        
        return score;
    }
    
    /**
     * Score multiple lines of output.
     */
    public MultiLineScore scoreMultipleOutputs(List<String> actuals, List<String> targets) {
        MultiLineScore multiScore = new MultiLineScore();
        
        if (actuals.size() != targets.size()) {
            multiScore.addError("Line count mismatch: expected " + targets.size() + " but got " + actuals.size());
        }
        
        int minSize = Math.min(actuals.size(), targets.size());
        
        for (int i = 0; i < minSize; i++) {
            LoggingScore lineScore = scoreOutput(actuals.get(i), targets.get(i));
            multiScore.addLineScore(lineScore);
            
            if (lineScore.isPerfect()) {
                multiScore.incrementPerfectLines();
            } else {
                multiScore.incrementImperfectLines();
                multiScore.addError("Line " + (i + 1) + ": " + String.join(", ", lineScore.getErrors()));
            }
        }
        
        // Calculate average score
        double totalScore = multiScore.getLineScores().stream()
            .mapToDouble(LoggingScore::getTotalScore)
            .average()
            .orElse(0.0);
            
        multiScore.setAverageScore(totalScore);
        
        return multiScore;
    }
    
    /**
     * Calculate string similarity using Levenshtein distance.
     */
    private double calculateStringSimilarity(String s1, String s2) {
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        return maxLength == 0 ? 1.0 : 1.0 - (double) distance / maxLength;
    }
    
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
}

/**
 * Score for a single line of logging output.
 */
class LoggingScore {
    private double totalScore = 0.0;
    private final java.util.Map<String, Double> componentScores = new java.util.HashMap<>();
    private final List<String> errors = new ArrayList<>();
    
    public boolean isPerfect() {
        return totalScore >= 100.0 && errors.isEmpty();
    }
    
    public void setTotalScore(double score) {
        this.totalScore = Math.min(100.0, Math.max(0.0, score));
    }
    
    public double getTotalScore() {
        return totalScore;
    }
    
    public void setComponentScore(String component, double score) {
        componentScores.put(component, Math.min(100.0, Math.max(0.0, score)));
    }
    
    public java.util.Map<String, Double> getComponentScores() {
        return componentScores;
    }
    
    public void addError(String error) {
        errors.add(error);
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    @Override
    public String toString() {
        return String.format("LoggingScore{total=%.1f%%, components=%s, errors=%s}", 
            totalScore, componentScores, errors);
    }
}

/**
 * Score for multiple lines of logging output.
 */
class MultiLineScore {
    private double averageScore = 0.0;
    private int perfectLines = 0;
    private int imperfectLines = 0;
    private final List<LoggingScore> lineScores = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    
    public boolean isPerfect() {
        return imperfectLines == 0 && errors.isEmpty();
    }
    
    public void setAverageScore(double score) {
        this.averageScore = score;
    }
    
    public double getAverageScore() {
        return averageScore;
    }
    
    public void incrementPerfectLines() {
        perfectLines++;
    }
    
    public void incrementImperfectLines() {
        imperfectLines++;
    }
    
    public int getPerfectLines() {
        return perfectLines;
    }
    
    public int getImperfectLines() {
        return imperfectLines;
    }
    
    public void addLineScore(LoggingScore score) {
        lineScores.add(score);
    }
    
    public List<LoggingScore> getLineScores() {
        return lineScores;
    }
    
    public void addError(String error) {
        errors.add(error);
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    @Override
    public String toString() {
        return String.format("MultiLineScore{average=%.1f%%, perfect=%d, imperfect=%d, errors=%s}", 
            averageScore, perfectLines, imperfectLines, errors);
    }
}