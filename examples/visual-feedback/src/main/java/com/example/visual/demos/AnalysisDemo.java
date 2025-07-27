package com.example.visual.demos;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.history.VisualizationOrchestrator;
import com.example.visual.analyzers.ActionAnalyzer;
import com.example.visual.analyzers.PerformanceTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates result analysis and visual feedback capabilities.
 * Shows how to analyze action results, generate insights, and
 * create detailed reports of automation execution.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisDemo {
    
    private final Action action;
    private final ActionAnalyzer actionAnalyzer;
    private final PerformanceTracker performanceTracker;
    private final VisualizationOrchestrator visualizationOrchestrator;
    private final ActionHistory actionHistory;
    
    public void runDemos() {
        log.info("\n=== Analysis Demo ===");
        
        // Demo 1: Basic result analysis
        demonstrateResultAnalysis();
        
        // Demo 2: Performance analysis
        demonstratePerformanceAnalysis();
        
        // Demo 3: Pattern recognition analysis
        demonstratePatternAnalysis();
        
        // Demo 4: Failure analysis
        demonstrateFailureAnalysis();
    }
    
    /**
     * Demo 1: Basic result analysis with confidence metrics
     */
    private void demonstrateResultAnalysis() {
        log.info("\n--- Demo 1: Result Analysis ---");
        
        // Create action with detailed analysis
        PatternFindOptions analyzedFind = new PatternFindOptions.Builder()
            .withAnalysis(true)
            .withVisualizationPath("result-analysis")
            .withBeforeActionLog("Starting analyzed search...")
            .withSuccessLog("Analysis complete - confidence: {confidence}%")
            .setAnalysisDepth("DETAILED")
            .setConfidenceAnalysis(true)
            .setPositionAnalysis(true)
            .setColorAnalysis(true)
            .then(new ClickOptions.Builder()
                .withAnalysis(true)
                .withBeforeActionLog("Analyzing click precision...")
                .withSuccessLog("Click analysis: offset ({offsetX}, {offsetY})")
                .setClickPrecisionAnalysis(true)
                .build())
            .build();
        
        // Simulate analysis results
        log.info("Result analysis features:");
        log.info("- Match confidence scoring and distribution");
        log.info("- Position accuracy and offset analysis");
        log.info("- Color space analysis for pattern matching");
        log.info("- Click precision and targeting accuracy");
        log.info("- Statistical confidence intervals");
        
        // Mock analysis output
        logAnalysisResults();
        
        log.info("✓ Result analysis demo configured");
    }
    
    /**
     * Demo 2: Performance analysis and benchmarking
     */
    private void demonstratePerformanceAnalysis() {
        log.info("\n--- Demo 2: Performance Analysis ---");
        
        // Create action with performance tracking
        ActionConfig performanceAction = new PatternFindOptions.Builder()
            .withPerformanceTracking(true)
            .withVisualizationPath("performance-analysis")
            .withBeforeActionLog("Starting performance-tracked action...")
            .withSuccessLog("Performance metrics captured")
            .setTrackMemoryUsage(true)
            .setTrackCpuUsage(true)
            .setTrackSearchTime(true)
            .setTrackNetworkLatency(false) // Local automation
            .then(new ClickOptions.Builder()
                .withPerformanceTracking(true)
                .withBeforeActionLog("Tracking click performance...")
                .withSuccessLog("Click performance recorded")
                .setTrackResponseTime(true)
                .build())
            .then(new TypeOptions.Builder()
                .withPerformanceTracking(true)
                .withBeforeActionLog("Tracking typing performance...")
                .withSuccessLog("Typing metrics captured")
                .setTrackInputSpeed(true)
                .setTrackInputAccuracy(true)
                .build())
            .build();
        
        // Performance benchmarking
        ActionConfig benchmarkAction = new PatternFindOptions.Builder()
            .withPerformanceTracking(true)
            .withBenchmarking(true)
            .withBeforeActionLog("Running performance benchmark...")
            .withSuccessLog("Benchmark complete - results available")
            .setBenchmarkIterations(10)
            .setBenchmarkWarmup(3)
            .setRecordDetailedMetrics(true)
            .build();
        
        log.info("Performance analysis features:");
        log.info("- Execution time distribution analysis");
        log.info("- Memory usage pattern tracking");
        log.info("- CPU utilization monitoring");
        log.info("- Search algorithm efficiency metrics");
        log.info("- Comparative benchmarking");
        
        // Mock performance data
        logPerformanceMetrics();
        
        log.info("✓ Performance analysis demo configured");
    }
    
    /**
     * Demo 3: Pattern recognition and matching analysis
     */
    private void demonstratePatternAnalysis() {
        log.info("\n--- Demo 3: Pattern Recognition Analysis ---");
        
        // Create action with pattern analysis
        PatternFindOptions patternAnalysis = new PatternFindOptions.Builder()
            .withPatternAnalysis(true)
            .withVisualizationPath("pattern-analysis")
            .withBeforeActionLog("Analyzing pattern matching algorithms...")
            .withSuccessLog("Pattern analysis complete")
            .setAnalyzeMatchingStrategy(true)
            .setAnalyzeColorSpaces(true)
            .setAnalyzeScaleVariance(true)
            .setAnalyzeRotationTolerance(true)
            .setGenerateHeatmap(true)
            .then(new PatternFindOptions.Builder()
                .withPatternAnalysis(true)
                .withBeforeActionLog("Comparing multiple pattern variants...")
                .withSuccessLog("Variant analysis complete")
                .setComparePatternVariants(true)
                .setAnalyzeFalsePositives(true)
                .build())
            .build();
        
        // Multi-resolution analysis
        PatternFindOptions resolutionAnalysis = new PatternFindOptions.Builder()
            .withPatternAnalysis(true)
            .withVisualizationPath("resolution-analysis")
            .withBeforeActionLog("Analyzing pattern at multiple resolutions...")
            .withSuccessLog("Resolution analysis complete")
            .setMultiResolutionAnalysis(true)
            .setScaleFactors(List.of(0.5, 0.75, 1.0, 1.25, 1.5))
            .setAnalyzeOptimalScale(true)
            .build();
        
        log.info("Pattern analysis features:");
        log.info("- Algorithm performance comparison (Template, Feature, etc.)");
        log.info("- Color space effectiveness analysis (RGB, HSV, Lab)");
        log.info("- Scale and rotation tolerance testing");
        log.info("- False positive detection and mitigation");
        log.info("- Pattern variant comparison and optimization");
        
        // Mock pattern analysis data
        logPatternAnalysisResults();
        
        log.info("✓ Pattern analysis demo configured");
    }
    
    /**
     * Demo 4: Failure analysis and debugging assistance
     */
    private void demonstrateFailureAnalysis() {
        log.info("\n--- Demo 4: Failure Analysis ---");
        
        // Create action with comprehensive failure analysis
        PatternFindOptions failureAnalysis = new PatternFindOptions.Builder()
            .withFailureAnalysis(true)
            .withVisualizationPath("failure-analysis")
            .withBeforeActionLog("Attempting action with failure analysis...")
            .withFailureLog("Action failed - detailed analysis available")
            .setCaptureFailureScreenshot(true)
            .setAnalyzeFailureCause(true)
            .setGenerateDebugReport(true)
            .setSuggestAlternatives(true)
            .setMaxRetries(3)
            .setAnalyzeRetryPatterns(true)
            .build();
        
        // Similarity threshold analysis
        PatternFindOptions thresholdAnalysis = new PatternFindOptions.Builder()
            .withFailureAnalysis(true)
            .withVisualizationPath("threshold-analysis")
            .withBeforeActionLog("Analyzing similarity thresholds...")
            .withSuccessLog("Optimal threshold identified")
            .withFailureLog("No suitable threshold found")
            .setAnalyzeSimilarityThresholds(true)
            .setThresholdRange(0.6, 0.95, 0.05) // Min, max, step
            .setGenerateThresholdChart(true)
            .build();
        
        // Scene change analysis
        PatternFindOptions sceneAnalysis = new PatternFindOptions.Builder()
            .withFailureAnalysis(true)
            .withVisualizationPath("scene-analysis")
            .withBeforeActionLog("Analyzing scene changes...")
            .withSuccessLog("Scene analysis complete")
            .setAnalyzeSceneChanges(true)
            .setDetectObstacles(true)
            .setAnalyzeLayoutShifts(true)
            .setTrackElementMovement(true)
            .build();
        
        log.info("Failure analysis features:");
        log.info("- Automatic failure cause identification");
        log.info("- Screenshot comparison with expected patterns");
        log.info("- Alternative action suggestions");
        log.info("- Similarity threshold optimization");
        log.info("- Scene change and obstacle detection");
        
        // Mock failure analysis data
        logFailureAnalysisResults();
        
        log.info("✓ Failure analysis demo configured");
    }
    
    /**
     * Helper: Log sample analysis results
     */
    private void logAnalysisResults() {
        log.info("Sample Analysis Results:");
        log.info("┌─────────────────────────────────────────┐");
        log.info("│ Match Confidence: 92.3%                │");
        log.info("│ Position: (450, 120)                   │");
        log.info("│ Offset from center: (+3, -1)           │");
        log.info("│ Color similarity: 89.7%                │");
        log.info("│ Pattern coverage: 85.2%                │");
        log.info("│ Search area: 1920x1080                 │");
        log.info("│ Match size: 120x30                     │");
        log.info("└─────────────────────────────────────────┘");
    }
    
    /**
     * Helper: Log sample performance metrics
     */
    private void logPerformanceMetrics() {
        log.info("Sample Performance Metrics:");
        log.info("┌─────────────────────────────────────────┐");
        log.info("│ Total execution time: 1,234ms          │");
        log.info("│ Search time: 892ms                     │");
        log.info("│ Pattern matching: 156ms                │");
        log.info("│ Image processing: 186ms                │");
        log.info("│ Memory usage: 45.2MB                   │");
        log.info("│ CPU usage peak: 23.4%                  │");
        log.info("│ Actions per second: 2.3                │");
        log.info("└─────────────────────────────────────────┘");
    }
    
    /**
     * Helper: Log sample pattern analysis results
     */
    private void logPatternAnalysisResults() {
        log.info("Sample Pattern Analysis:");
        log.info("┌─────────────────────────────────────────┐");
        log.info("│ Best algorithm: Template Matching      │");
        log.info("│ Optimal color space: HSV               │");
        log.info("│ Scale tolerance: ±15%                  │");
        log.info("│ Rotation tolerance: ±5°                │");
        log.info("│ False positive rate: 2.1%              │");
        log.info("│ Pattern variants tested: 8              │");
        log.info("│ Recommended threshold: 0.87             │");
        log.info("└─────────────────────────────────────────┘");
    }
    
    /**
     * Helper: Log sample failure analysis results
     */
    private void logFailureAnalysisResults() {
        log.info("Sample Failure Analysis:");
        log.info("┌─────────────────────────────────────────┐");
        log.info("│ Failure cause: Element obscured        │");
        log.info("│ Obstruction: Modal dialog overlay      │");
        log.info("│ Suggested action: Close modal first    │");
        log.info("│ Alternative patterns: 3 found          │");
        log.info("│ Optimal threshold: 0.82 (was 0.90)     │");
        log.info("│ Scene change detected: Yes              │");
        log.info("│ Recovery success rate: 78%             │");
        log.info("└─────────────────────────────────────────┘");
    }
    
    /**
     * Helper: Demonstrate custom analysis workflow
     */
    private void demonstrateCustomAnalysis() {
        log.info("\n--- Custom Analysis Workflow ---");
        
        // Get recent action history
        List<ActionResult> recentActions = actionHistory.getRecentActions(10);
        
        // Analyze patterns in the history
        Map<String, Object> trends = actionAnalyzer.analyzeTrends(recentActions);
        
        // Generate performance report
        String performanceReport = performanceTracker.generateReport(
            Duration.ofMinutes(30) // Last 30 minutes
        );
        
        // Create visualization
        visualizationOrchestrator.generateAnalysisVisualization(
            recentActions, 
            "custom-analysis",
            Map.of(
                "showTrends", true,
                "includePerformance", true,
                "highlightFailures", true
            )
        );
        
        log.info("Custom analysis features:");
        log.info("- Historical trend analysis");
        log.info("- Custom performance reporting");
        log.info("- Interactive visualization generation");
        log.info("- Automated insight discovery");
        
        log.info("✓ Custom analysis workflow configured");
    }
}