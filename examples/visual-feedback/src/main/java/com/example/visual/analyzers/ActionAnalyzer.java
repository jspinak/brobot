package com.example.visual.analyzers;

import io.github.jspinak.brobot.action.ActionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Custom analyzer for action results and performance patterns.
 * Provides detailed analysis of automation execution including
 * confidence trends, performance patterns, and optimization recommendations.
 */
@Component
@Slf4j
public class ActionAnalyzer {
    
    /**
     * Analyzes a single action result with detailed metrics
     */
    public AnalysisResult analyze(ActionResult result, BufferedImage screenshot) {
        log.debug("Analyzing action result: {}", result.getActionDescription());
        
        // Calculate basic metrics
        double confidence = result.isSuccess() && result.getBestMatch() != null 
            ? result.getBestMatch().getScore() : 0.0;
        Duration executionTime = result.getDuration();
        
        // Analyze screenshot if provided
        ImageAnalysis imageAnalysis = screenshot != null 
            ? analyzeScreenshot(screenshot) : ImageAnalysis.empty();
        
        // Calculate custom metrics
        Map<String, Object> customMetrics = calculateCustomMetrics(result, screenshot);
        
        return AnalysisResult.builder()
            .actionId(result.getActionId())
            .success(result.isSuccess())
            .confidence(confidence)
            .executionTime(executionTime)
            .imageAnalysis(imageAnalysis)
            .customMetrics(customMetrics)
            .timestamp(result.getTimestamp())
            .recommendations(generateRecommendations(result, confidence))
            .build();
    }
    
    /**
     * Analyzes trends across multiple action results
     */
    public Map<String, Object> analyzeTrends(List<ActionResult> results) {
        log.info("Analyzing trends across {} action results", results.size());
        
        Map<String, Object> trends = new HashMap<>();
        
        if (results.isEmpty()) {
            return trends;
        }
        
        // Success rate trend
        double successRate = results.stream()
            .mapToDouble(r -> r.isSuccess() ? 1.0 : 0.0)
            .average()
            .orElse(0.0);
        trends.put("successRate", successRate);
        
        // Average confidence trend
        double avgConfidence = results.stream()
            .filter(ActionResult::isSuccess)
            .filter(r -> r.getBestMatch() != null)
            .mapToDouble(r -> r.getBestMatch().getScore())
            .average()
            .orElse(0.0);
        trends.put("averageConfidence", avgConfidence);
        
        // Performance trend
        double avgExecutionTime = results.stream()
            .mapToLong(r -> r.getDuration().toMillis())
            .average()
            .orElse(0.0);
        trends.put("averageExecutionTime", avgExecutionTime);
        
        // Error patterns
        Map<String, Long> errorPatterns = results.stream()
            .filter(r -> !r.isSuccess())
            .collect(Collectors.groupingBy(
                r -> r.getErrorMessage() != null ? r.getErrorMessage() : "Unknown",
                Collectors.counting()
            ));
        trends.put("errorPatterns", errorPatterns);
        
        // Performance degradation detection
        boolean performanceDegrading = detectPerformanceDegradation(results);
        trends.put("performanceDegrading", performanceDegrading);
        
        // Confidence degradation detection
        boolean confidenceDegrading = detectConfidenceDegradation(results);
        trends.put("confidenceDegrading", confidenceDegrading);
        
        log.info("Trend analysis complete: Success rate: {:.1f}%, Avg confidence: {:.1f}%",
            successRate * 100, avgConfidence * 100);
        
        return trends;
    }
    
    /**
     * Analyzes screenshot for visual patterns and elements
     */
    private ImageAnalysis analyzeScreenshot(BufferedImage screenshot) {
        // Basic image analysis
        int width = screenshot.getWidth();
        int height = screenshot.getHeight();
        
        // Color analysis
        ColorDistribution colorDist = analyzeColorDistribution(screenshot);
        
        // Complexity analysis
        double complexity = calculateImageComplexity(screenshot);
        
        // Edge detection for UI element density
        int edgeCount = detectEdges(screenshot);
        
        return ImageAnalysis.builder()
            .width(width)
            .height(height)
            .colorDistribution(colorDist)
            .complexity(complexity)
            .edgeCount(edgeCount)
            .estimatedUIElements(estimateUIElementCount(edgeCount, width, height))
            .build();
    }
    
    /**
     * Calculates custom metrics specific to the action type
     */
    private Map<String, Object> calculateCustomMetrics(ActionResult result, BufferedImage screenshot) {
        Map<String, Object> metrics = new HashMap<>();
        
        // Search efficiency metric
        if (result.getSearchTime() != null) {
            double searchEfficiency = calculateSearchEfficiency(result);
            metrics.put("searchEfficiency", searchEfficiency);
        }
        
        // Match quality score
        if (result.getBestMatch() != null) {
            double matchQuality = calculateMatchQuality(result.getBestMatch());
            metrics.put("matchQuality", matchQuality);
        }
        
        // Resource utilization
        if (result.getMemoryUsage() != null) {
            metrics.put("memoryEfficiency", calculateMemoryEfficiency(result));
        }
        
        // Screenshot quality (if available)
        if (screenshot != null) {
            metrics.put("screenshotQuality", assessScreenshotQuality(screenshot));
        }
        
        return metrics;
    }
    
    /**
     * Generates recommendations based on analysis results
     */
    private List<String> generateRecommendations(ActionResult result, double confidence) {
        List<String> recommendations = new java.util.ArrayList<>();
        
        // Confidence-based recommendations
        if (confidence < 0.8) {
            recommendations.add("Consider adjusting similarity threshold or updating pattern images");
        }
        
        // Performance-based recommendations
        if (result.getDuration().toMillis() > 5000) {
            recommendations.add("Action took longer than expected - consider optimizing search area");
        }
        
        // Failure-based recommendations
        if (!result.isSuccess()) {
            recommendations.add("Action failed - verify element visibility and accessibility");
        }
        
        // Search-specific recommendations
        if (result.getSearchTime() != null && result.getSearchTime().toMillis() > 3000) {
            recommendations.add("Search time is high - consider using region-based searching");
        }
        
        return recommendations;
    }
    
    /**
     * Detects performance degradation trends
     */
    private boolean detectPerformanceDegradation(List<ActionResult> results) {
        if (results.size() < 5) return false;
        
        // Compare recent performance with earlier performance
        int recentCount = Math.min(results.size() / 3, 10);
        
        double recentAvgTime = results.stream()
            .skip(results.size() - recentCount)
            .mapToLong(r -> r.getDuration().toMillis())
            .average()
            .orElse(0.0);
            
        double earlierAvgTime = results.stream()
            .limit(recentCount)
            .mapToLong(r -> r.getDuration().toMillis())
            .average()
            .orElse(0.0);
        
        // Performance degrading if recent is >20% slower
        return recentAvgTime > earlierAvgTime * 1.2;
    }
    
    /**
     * Detects confidence degradation trends
     */
    private boolean detectConfidenceDegradation(List<ActionResult> results) {
        if (results.size() < 5) return false;
        
        int recentCount = Math.min(results.size() / 3, 10);
        
        double recentAvgConfidence = results.stream()
            .skip(results.size() - recentCount)
            .filter(ActionResult::isSuccess)
            .filter(r -> r.getBestMatch() != null)
            .mapToDouble(r -> r.getBestMatch().getScore())
            .average()
            .orElse(0.0);
            
        double earlierAvgConfidence = results.stream()
            .limit(recentCount)
            .filter(ActionResult::isSuccess)
            .filter(r -> r.getBestMatch() != null)
            .mapToDouble(r -> r.getBestMatch().getScore())
            .average()
            .orElse(0.0);
        
        // Confidence degrading if recent is >10% lower
        return recentAvgConfidence < earlierAvgConfidence * 0.9;
    }
    
    /**
     * Analyzes color distribution in screenshot
     */
    private ColorDistribution analyzeColorDistribution(BufferedImage image) {
        Map<String, Integer> colorCounts = new HashMap<>();
        
        // Sample pixels for performance
        int sampleRate = Math.max(1, image.getWidth() * image.getHeight() / 10000);
        
        for (int y = 0; y < image.getHeight(); y += sampleRate) {
            for (int x = 0; x < image.getWidth(); x += sampleRate) {
                int rgb = image.getRGB(x, y);
                String colorCategory = categorizeColor(rgb);
                colorCounts.merge(colorCategory, 1, Integer::sum);
            }
        }
        
        return new ColorDistribution(colorCounts);
    }
    
    /**
     * Calculates image complexity based on edge density
     */
    private double calculateImageComplexity(BufferedImage image) {
        // Simplified complexity calculation
        int edgeCount = detectEdges(image);
        int totalPixels = image.getWidth() * image.getHeight();
        return (double) edgeCount / totalPixels;
    }
    
    /**
     * Simple edge detection for UI analysis
     */
    private int detectEdges(BufferedImage image) {
        int edgeCount = 0;
        int threshold = 50; // Brightness difference threshold
        
        for (int y = 1; y < image.getHeight() - 1; y++) {
            for (int x = 1; x < image.getWidth() - 1; x++) {
                int center = getBrightness(image.getRGB(x, y));
                int right = getBrightness(image.getRGB(x + 1, y));
                int down = getBrightness(image.getRGB(x, y + 1));
                
                if (Math.abs(center - right) > threshold || 
                    Math.abs(center - down) > threshold) {
                    edgeCount++;
                }
            }
        }
        
        return edgeCount;
    }
    
    /**
     * Helper methods for analysis calculations
     */
    private double calculateSearchEfficiency(ActionResult result) {
        if (result.getSearchTime() == null) return 1.0;
        
        long searchMs = result.getSearchTime().toMillis();
        long totalMs = result.getDuration().toMillis();
        
        return 1.0 - ((double) searchMs / totalMs);
    }
    
    private double calculateMatchQuality(Object bestMatch) {
        // Simplified match quality calculation
        // In real implementation, would analyze match properties
        return 0.85; // Mock value
    }
    
    private double calculateMemoryEfficiency(ActionResult result) {
        // Simplified memory efficiency calculation
        return 0.90; // Mock value
    }
    
    private double assessScreenshotQuality(BufferedImage screenshot) {
        // Assess screenshot quality based on clarity, contrast, etc.
        return 0.88; // Mock value
    }
    
    private String categorizeColor(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        
        // Simple color categorization
        if (r > 200 && g > 200 && b > 200) return "light";
        if (r < 50 && g < 50 && b < 50) return "dark";
        if (r > g && r > b) return "red";
        if (g > r && g > b) return "green";
        if (b > r && b > g) return "blue";
        return "neutral";
    }
    
    private int getBrightness(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r + g + b) / 3;
    }
    
    private int estimateUIElementCount(int edgeCount, int width, int height) {
        // Rough estimation based on edge density
        double edgeDensity = (double) edgeCount / (width * height);
        return (int) (edgeDensity * 1000); // Scaling factor
    }
    
    // Data classes for analysis results
    public static class AnalysisResult {
        public static AnalysisResultBuilder builder() {
            return new AnalysisResultBuilder();
        }
        
        public static class AnalysisResultBuilder {
            public AnalysisResultBuilder actionId(String actionId) { return this; }
            public AnalysisResultBuilder success(boolean success) { return this; }
            public AnalysisResultBuilder confidence(double confidence) { return this; }
            public AnalysisResultBuilder executionTime(Duration duration) { return this; }
            public AnalysisResultBuilder imageAnalysis(ImageAnalysis analysis) { return this; }
            public AnalysisResultBuilder customMetrics(Map<String, Object> metrics) { return this; }
            public AnalysisResultBuilder timestamp(Object timestamp) { return this; }
            public AnalysisResultBuilder recommendations(List<String> recommendations) { return this; }
            public AnalysisResult build() { return new AnalysisResult(); }
        }
    }
    
    public static class ImageAnalysis {
        public static ImageAnalysisBuilder builder() {
            return new ImageAnalysisBuilder();
        }
        
        public static ImageAnalysis empty() {
            return new ImageAnalysis();
        }
        
        public static class ImageAnalysisBuilder {
            public ImageAnalysisBuilder width(int width) { return this; }
            public ImageAnalysisBuilder height(int height) { return this; }
            public ImageAnalysisBuilder colorDistribution(ColorDistribution dist) { return this; }
            public ImageAnalysisBuilder complexity(double complexity) { return this; }
            public ImageAnalysisBuilder edgeCount(int count) { return this; }
            public ImageAnalysisBuilder estimatedUIElements(int count) { return this; }
            public ImageAnalysis build() { return new ImageAnalysis(); }
        }
    }
    
    public static class ColorDistribution {
        private final Map<String, Integer> distribution;
        
        public ColorDistribution(Map<String, Integer> distribution) {
            this.distribution = distribution;
        }
        
        public Map<String, Integer> getDistribution() {
            return distribution;
        }
    }
}