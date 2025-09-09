package io.github.jspinak.brobot.diagnostics;

import lombok.extern.slf4j.Slf4j;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Analyzes pattern matching quality to diagnose matching issues and optimize thresholds.
 * 
 * <p>This diagnostic tool helps identify and resolve common pattern matching problems:
 * <ul>
 *   <li>Similarity threshold issues (too high/too low)</li>
 *   <li>Image quality problems</li>
 *   <li>Resolution and DPI mismatches</li>
 *   <li>False positive detection</li>
 *   <li>Pattern ambiguity (multiple similar matches)</li>
 *   <li>Screen capture problems</li>
 * </ul>
 * </p>
 * 
 * <p>The analyzer provides detailed diagnostics including:
 * <ul>
 *   <li>Match quality distribution analysis</li>
 *   <li>Clustering detection for multiple matches</li>
 *   <li>Confidence score evaluation</li>
 *   <li>Recommendations for threshold adjustments</li>
 *   <li>Warnings about potential issues</li>
 * </ul>
 * </p>
 * 
 * <p>Usage example:
 * <pre>{@code
 * MatchQualityAnalyzer analyzer = new MatchQualityAnalyzer();
 * ActionResult result = action.find(pattern);
 * analyzer.analyzeMatches("MyPattern", result);
 * 
 * // Get detailed report
 * String report = analyzer.generateReport("MyPattern", result);
 * }</pre>
 * </p>
 * 
 * @since 1.0
 */
@Slf4j
public class MatchQualityAnalyzer {
    
    // Default thresholds for analysis
    private static final double DEFAULT_GOOD_MATCH_THRESHOLD = 0.8;
    private static final double DEFAULT_ACCEPTABLE_MATCH_THRESHOLD = 0.7;
    private static final double DEFAULT_SUSPICIOUS_MATCH_THRESHOLD = 0.5;
    private static final int DEFAULT_MAX_EXPECTED_MATCHES = 5;
    private static final double DEFAULT_CLUSTER_DISTANCE = 50.0; // pixels
    
    private final double goodMatchThreshold;
    private final double acceptableMatchThreshold;
    private final double suspiciousMatchThreshold;
    private final int maxExpectedMatches;
    private final double clusterDistance;
    
    /**
     * Creates an analyzer with default thresholds.
     */
    public MatchQualityAnalyzer() {
        this(DEFAULT_GOOD_MATCH_THRESHOLD, 
             DEFAULT_ACCEPTABLE_MATCH_THRESHOLD,
             DEFAULT_SUSPICIOUS_MATCH_THRESHOLD,
             DEFAULT_MAX_EXPECTED_MATCHES,
             DEFAULT_CLUSTER_DISTANCE);
    }
    
    /**
     * Creates an analyzer with custom thresholds.
     * 
     * @param goodMatchThreshold Threshold for good quality matches (e.g., 0.8)
     * @param acceptableMatchThreshold Threshold for acceptable matches (e.g., 0.7)
     * @param suspiciousMatchThreshold Threshold below which matches are suspicious (e.g., 0.5)
     * @param maxExpectedMatches Maximum number of matches expected for a pattern
     * @param clusterDistance Distance in pixels to consider matches as clustered
     */
    public MatchQualityAnalyzer(double goodMatchThreshold, 
                                double acceptableMatchThreshold,
                                double suspiciousMatchThreshold,
                                int maxExpectedMatches,
                                double clusterDistance) {
        this.goodMatchThreshold = goodMatchThreshold;
        this.acceptableMatchThreshold = acceptableMatchThreshold;
        this.suspiciousMatchThreshold = suspiciousMatchThreshold;
        this.maxExpectedMatches = maxExpectedMatches;
        this.clusterDistance = clusterDistance;
    }
    
    /**
     * Analyzes the quality of pattern matches and logs diagnostic information.
     * 
     * @param patternName Name of the pattern being matched
     * @param result The action result containing matches
     */
    public void analyzeMatches(String patternName, ActionResult result) {
        if (result == null || result.getMatchList() == null) {
            log.warn("[MATCH ANALYSIS] No results to analyze for pattern '{}'", patternName);
            return;
        }
        
        List<Match> matches = result.getMatchList();
        
        log.info("=== MATCH QUALITY ANALYSIS for '{}' ===", patternName);
        log.info("Total matches found: {}", matches.size());
        
        if (matches.isEmpty()) {
            logNoMatchesFound(patternName);
            return;
        }
        
        analyzeMatchQuality(matches);
        analyzeMatchDistribution(matches);
        detectClusters(matches);
        provideRecommendations(matches);
    }
    
    /**
     * Generates a detailed analysis report as a string.
     * 
     * @param patternName Name of the pattern being matched
     * @param result The action result containing matches
     * @return A detailed analysis report
     */
    public String generateReport(String patternName, ActionResult result) {
        StringBuilder report = new StringBuilder();
        report.append(String.format("=== MATCH QUALITY REPORT for '%s' ===%n", patternName));
        
        if (result == null || result.getMatchList() == null || result.getMatchList().isEmpty()) {
            report.append("No matches found.\n");
            report.append(getNoMatchRecommendations());
            return report.toString();
        }
        
        List<Match> matches = result.getMatchList();
        report.append(String.format("Total matches: %d%n", matches.size()));
        
        // Quality distribution
        report.append("\nQuality Distribution:\n");
        report.append(getQualityDistribution(matches));
        
        // Match details
        report.append("\nMatch Details:\n");
        for (int i = 0; i < Math.min(matches.size(), 10); i++) {
            Match match = matches.get(i);
            report.append(String.format("  [%d] Score: %.3f, Location: (%d, %d), Size: %dx%d%n",
                i + 1, match.getScore(), match.x(), match.y(), match.w(), match.h()));
        }
        
        // Clustering analysis
        if (matches.size() > 1) {
            report.append("\nClustering Analysis:\n");
            report.append(getClusterAnalysis(matches));
        }
        
        // Recommendations
        report.append("\nRecommendations:\n");
        report.append(getRecommendations(matches));
        
        return report.toString();
    }
    
    private void logNoMatchesFound(String patternName) {
        log.warn("NO MATCHES FOUND - Possible causes:");
        log.warn("  1. Pattern image doesn't exist on screen");
        log.warn("  2. Similarity threshold too high");
        log.warn("  3. Screen capture is black/corrupted");
        log.warn("  4. Wrong screen/window being captured");
        log.warn("  5. Pattern captured at different resolution/DPI");
        log.warn("  6. Pattern has transparency issues");
        log.warn("  7. Color depth mismatch (24-bit vs 32-bit)");
    }
    
    private void analyzeMatchQuality(List<Match> matches) {
        List<Match> goodMatches = matches.stream()
            .filter(m -> m.getScore() >= goodMatchThreshold)
            .collect(Collectors.toList());
        
        List<Match> acceptableMatches = matches.stream()
            .filter(m -> m.getScore() >= acceptableMatchThreshold && m.getScore() < goodMatchThreshold)
            .collect(Collectors.toList());
        
        List<Match> poorMatches = matches.stream()
            .filter(m -> m.getScore() < acceptableMatchThreshold && m.getScore() >= suspiciousMatchThreshold)
            .collect(Collectors.toList());
        
        List<Match> suspiciousMatches = matches.stream()
            .filter(m -> m.getScore() < suspiciousMatchThreshold)
            .collect(Collectors.toList());
        
        log.info("Match Quality Distribution:");
        log.info("  - Good (>= {}): {} matches", goodMatchThreshold, goodMatches.size());
        log.info("  - Acceptable (>= {}): {} matches", acceptableMatchThreshold, acceptableMatches.size());
        log.info("  - Poor (>= {}): {} matches", suspiciousMatchThreshold, poorMatches.size());
        log.info("  - Suspicious (< {}): {} matches", suspiciousMatchThreshold, suspiciousMatches.size());
        
        if (!suspiciousMatches.isEmpty()) {
            log.warn("WARNING: {} suspicious matches detected with very low confidence", suspiciousMatches.size());
        }
    }
    
    private void analyzeMatchDistribution(List<Match> matches) {
        if (matches.size() > maxExpectedMatches) {
            log.warn("WARNING: Found {} matches, expected at most {}. Possible issues:",
                matches.size(), maxExpectedMatches);
            log.warn("  - Pattern is too generic/simple");
            log.warn("  - Multiple similar UI elements on screen");
            log.warn("  - Similarity threshold too low");
        }
        
        // Analyze score variance
        double avgScore = matches.stream()
            .mapToDouble(Match::getScore)
            .average()
            .orElse(0);
        
        double variance = matches.stream()
            .mapToDouble(m -> Math.pow(m.getScore() - avgScore, 2))
            .average()
            .orElse(0);
        
        log.info("Score Statistics:");
        log.info("  - Average: {:.3f}", avgScore);
        log.info("  - Variance: {:.3f}", variance);
        
        if (variance > 0.01) {
            log.info("  - High variance detected - matches have inconsistent quality");
        }
    }
    
    private void detectClusters(List<Match> matches) {
        if (matches.size() <= 1) return;
        
        int clusters = 0;
        for (int i = 0; i < matches.size(); i++) {
            Match m1 = matches.get(i);
            boolean inCluster = false;
            
            for (int j = i + 1; j < matches.size(); j++) {
                Match m2 = matches.get(j);
                double distance = calculateDistance(m1, m2);
                
                if (distance < clusterDistance) {
                    inCluster = true;
                    log.debug("Matches {} and {} are clustered (distance: {:.1f} pixels)",
                        i, j, distance);
                }
            }
            
            if (inCluster) clusters++;
        }
        
        if (clusters > 0) {
            log.info("Detected {} clustered matches - possible duplicate detections", clusters);
        }
    }
    
    private double calculateDistance(Match m1, Match m2) {
        double dx = m1.x() - m2.x();
        double dy = m1.y() - m2.y();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    private void provideRecommendations(List<Match> matches) {
        log.info("=== RECOMMENDATIONS ===");
        
        double avgScore = matches.stream()
            .mapToDouble(Match::getScore)
            .average()
            .orElse(0);
        
        if (avgScore < acceptableMatchThreshold) {
            log.info("• Consider lowering similarity threshold to {:.2f}",
                Math.max(avgScore - 0.1, 0.5));
        }
        
        if (matches.size() > maxExpectedMatches) {
            log.info("• Pattern may be too generic - consider using more distinctive patterns");
            log.info("• Increase similarity threshold to filter false positives");
        }
        
        if (avgScore > 0.95 && matches.size() == 1) {
            log.info("• Excellent match quality - pattern is working well");
        }
        
        if (matches.stream().anyMatch(m -> m.getScore() < suspiciousMatchThreshold)) {
            log.info("• Remove suspicious matches by setting minimum threshold to {}",
                suspiciousMatchThreshold);
        }
    }
    
    private String getNoMatchRecommendations() {
        return """
            Troubleshooting steps:
            1. Verify the pattern image exists in the correct location
            2. Check if the UI element is visible on screen
            3. Try lowering the similarity threshold (current might be too strict)
            4. Ensure screen capture is working (not black/corrupted)
            5. Check for DPI/resolution differences between pattern and screen
            6. Verify color depth compatibility (24-bit vs 32-bit)
            7. Test with a simpler, more distinctive pattern
            """;
    }
    
    private String getQualityDistribution(List<Match> matches) {
        long good = matches.stream().filter(m -> m.getScore() >= goodMatchThreshold).count();
        long acceptable = matches.stream().filter(m -> m.getScore() >= acceptableMatchThreshold).count();
        long poor = matches.stream().filter(m -> m.getScore() < acceptableMatchThreshold).count();
        
        return String.format("  Good (>= %.2f): %d%n  Acceptable (>= %.2f): %d%n  Poor (< %.2f): %d%n",
            goodMatchThreshold, good, acceptableMatchThreshold, acceptable - good, acceptableMatchThreshold, poor);
    }
    
    private String getClusterAnalysis(List<Match> matches) {
        StringBuilder analysis = new StringBuilder();
        int clustered = 0;
        
        for (int i = 0; i < matches.size() - 1; i++) {
            for (int j = i + 1; j < matches.size(); j++) {
                double distance = calculateDistance(matches.get(i), matches.get(j));
                if (distance < clusterDistance) {
                    clustered++;
                }
            }
        }
        
        if (clustered > 0) {
            analysis.append(String.format("  Found %d clustered match pairs (within %.1f pixels)%n",
                clustered, clusterDistance));
            analysis.append("  This may indicate duplicate detections or overlapping patterns\n");
        } else {
            analysis.append("  No clustering detected - matches are well distributed\n");
        }
        
        return analysis.toString();
    }
    
    private String getRecommendations(List<Match> matches) {
        StringBuilder recommendations = new StringBuilder();
        double avgScore = matches.stream().mapToDouble(Match::getScore).average().orElse(0);
        
        if (avgScore < acceptableMatchThreshold) {
            recommendations.append(String.format("  • Lower similarity threshold to %.2f%n", 
                Math.max(avgScore - 0.1, 0.5)));
        }
        
        if (matches.size() > maxExpectedMatches) {
            recommendations.append("  • Use more distinctive patterns\n");
            recommendations.append("  • Increase similarity threshold\n");
        }
        
        if (avgScore > 0.95) {
            recommendations.append("  • Pattern matching is excellent\n");
        }
        
        return recommendations.toString();
    }
}