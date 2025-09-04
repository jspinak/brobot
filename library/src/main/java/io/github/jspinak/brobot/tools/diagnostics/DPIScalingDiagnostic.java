package io.github.jspinak.brobot.tools.diagnostics;

import io.github.jspinak.brobot.dpi.DPIScalingStrategy;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Diagnostic tool for analyzing and resolving DPI scaling issues.
 * Helps determine why patterns that match in SikuliX IDE don't match in Brobot.
 * 
 * Key findings from testing:
 * - SikuliX IDE captures patterns in physical pixels
 * - Windows snipping tool captures in logical pixels  
 * - With 125% Windows scaling: logical 100px = physical 125px
 * - Pattern scale factor = 1/display_scale (e.g., 0.8 for 125%)
 */
public class DPIScalingDiagnostic {
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    
    public static class DiagnosticResult {
        public final String patternPath;
        public final int width;
        public final int height;
        public final DPIScalingStrategy.PatternSource likelySource;
        public final double bestSimilarity;
        public final float bestDPISetting;
        public final String recommendation;
        
        public DiagnosticResult(String patternPath, int width, int height,
                               DPIScalingStrategy.PatternSource likelySource,
                               double bestSimilarity, float bestDPISetting,
                               String recommendation) {
            this.patternPath = patternPath;
            this.width = width;
            this.height = height;
            this.likelySource = likelySource;
            this.bestSimilarity = bestSimilarity;
            this.bestDPISetting = bestDPISetting;
            this.recommendation = recommendation;
        }
    }
    
    /**
     * Analyzes a pattern and determines optimal DPI settings
     */
    public static DiagnosticResult analyzePattern(String patternPath) {
        System.out.println("\n=== DPI SCALING DIAGNOSTIC ===");
        System.out.println("Pattern: " + patternPath);
        System.out.println("Time: " + dateFormat.format(new Date()));
        System.out.println("=" + "=".repeat(50));
        
        File patternFile = new File(patternPath);
        if (!patternFile.exists()) {
            System.err.println("ERROR: Pattern file not found: " + patternPath);
            return null;
        }
        
        try {
            // Load and analyze pattern
            BufferedImage patternImage = ImageIO.read(patternFile);
            int width = patternImage.getWidth();
            int height = patternImage.getHeight();
            
            System.out.println("\nPATTERN ANALYSIS:");
            System.out.println("  Dimensions: " + width + "x" + height);
            System.out.println("  Image type: " + getImageTypeString(patternImage.getType()));
            
            // Detect display scaling
            double displayScale = DPIScalingStrategy.detectDisplayScaling();
            System.out.println("\nDISPLAY CONFIGURATION:");
            System.out.println("  Display scaling: " + (int)(displayScale * 100) + "%");
            System.out.println("  Physical/Logical ratio: " + displayScale);
            
            // Guess pattern source based on dimensions
            DPIScalingStrategy.PatternSource likelySource = guessPatternSource(width, height, displayScale);
            System.out.println("  Likely pattern source: " + likelySource);
            
            // Test different DPI settings
            System.out.println("\nTESTING DPI SETTINGS:");
            System.out.println("-".repeat(50));
            
            Screen screen = new Screen();
            Settings.MinSimilarity = 0.3; // Low threshold for testing
            
            double bestSimilarity = 0;
            float bestDPISetting = 1.0f;
            
            // Test various DPI settings
            float[] testSettings = {1.0f, 0.9f, 0.8f, 0.75f, 0.67f, 0.6f, 0.5f};
            
            for (float dpiSetting : testSettings) {
                Settings.AlwaysResize = dpiSetting;
                
                try {
                    Pattern pattern = new Pattern(patternPath).similar(0.3);
                    Match match = screen.exists(pattern, 0.5);
                    
                    if (match != null) {
                        double similarity = match.getScore();
                        System.out.printf("  DPI %.2f: %.1f%% similarity", dpiSetting, similarity * 100);
                        
                        if (similarity > bestSimilarity) {
                            bestSimilarity = similarity;
                            bestDPISetting = dpiSetting;
                            System.out.print(" ← BEST");
                        }
                        
                        if (similarity > 0.90) {
                            System.out.print(" ✅ EXCELLENT");
                        } else if (similarity > 0.80) {
                            System.out.print(" ✓ Very Good");
                        } else if (similarity > 0.70) {
                            System.out.print(" ⚠ Good");
                        }
                        System.out.println();
                    } else {
                        System.out.printf("  DPI %.2f: No match found%n", dpiSetting);
                    }
                } catch (Exception e) {
                    System.err.printf("  DPI %.2f: Error - %s%n", dpiSetting, e.getMessage());
                }
            }
            
            // Generate recommendation
            String recommendation = generateRecommendation(
                likelySource, displayScale, bestSimilarity, bestDPISetting
            );
            
            System.out.println("\n" + "=".repeat(50));
            System.out.println("RESULTS:");
            System.out.println("  Best DPI setting: " + bestDPISetting);
            System.out.println("  Best similarity: " + String.format("%.1f%%", bestSimilarity * 100));
            System.out.println("\nRECOMMENDATION:");
            System.out.println(recommendation);
            System.out.println("=".repeat(50));
            
            return new DiagnosticResult(
                patternPath, width, height, likelySource,
                bestSimilarity, bestDPISetting, recommendation
            );
            
        } catch (IOException e) {
            System.err.println("ERROR: Could not read pattern file: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Analyzes multiple patterns and provides consolidated recommendations
     */
    public static void analyzePatterns(List<String> patternPaths) {
        List<DiagnosticResult> results = new ArrayList<>();
        
        System.out.println("\n=== ANALYZING MULTIPLE PATTERNS ===");
        
        for (String path : patternPaths) {
            DiagnosticResult result = analyzePattern(path);
            if (result != null) {
                results.add(result);
            }
        }
        
        if (!results.isEmpty()) {
            System.out.println("\n=== CONSOLIDATED RECOMMENDATIONS ===");
            
            // Find most common best DPI setting
            float commonDPI = findMostCommonDPI(results);
            double avgSimilarity = results.stream()
                .mapToDouble(r -> r.bestSimilarity)
                .average()
                .orElse(0);
            
            System.out.println("Across " + results.size() + " patterns:");
            System.out.println("  Most common optimal DPI: " + commonDPI);
            System.out.println("  Average best similarity: " + String.format("%.1f%%", avgSimilarity * 100));
            
            System.out.println("\nRECOMMENDED CONFIGURATION:");
            System.out.println("  brobot.dpi.resize.factor=" + commonDPI);
            
            if (avgSimilarity < 0.70) {
                System.out.println("\n⚠ WARNING: Low average similarity detected!");
                System.out.println("  Consider recapturing patterns with consistent tool");
                System.out.println("  - Use SikuliX IDE for all patterns, OR");
                System.out.println("  - Use Windows snipping tool for all patterns");
            }
        }
    }
    
    private static DPIScalingStrategy.PatternSource guessPatternSource(
            int width, int height, double displayScale) {
        
        // Common logical sizes for UI elements
        int[][] commonLogicalSizes = {
            {100, 50}, {150, 75}, {200, 100}, {103, 60}, {195, 80}
        };
        
        // Check if dimensions match common logical sizes
        for (int[] size : commonLogicalSizes) {
            if (Math.abs(width - size[0]) <= 5 && Math.abs(height - size[1]) <= 5) {
                return DPIScalingStrategy.PatternSource.WINDOWS_TOOL;
            }
            
            // Check if dimensions match scaled physical sizes
            int physicalWidth = (int)(size[0] * displayScale);
            int physicalHeight = (int)(size[1] * displayScale);
            
            if (Math.abs(width - physicalWidth) <= 5 && Math.abs(height - physicalHeight) <= 5) {
                return DPIScalingStrategy.PatternSource.SIKULI_IDE;
            }
        }
        
        // Default assumption
        return DPIScalingStrategy.PatternSource.SIKULI_IDE;
    }
    
    private static String generateRecommendation(
            DPIScalingStrategy.PatternSource source,
            double displayScale,
            double bestSimilarity,
            float bestDPISetting) {
        
        StringBuilder sb = new StringBuilder();
        
        if (bestSimilarity > 0.90) {
            sb.append("✅ Pattern matching is working well!\n");
            sb.append("  Use Settings.AlwaysResize = ").append(bestDPISetting).append("\n");
        } else if (bestSimilarity > 0.70) {
            sb.append("⚠ Pattern matching is acceptable but could be improved.\n");
            sb.append("  Current best: Settings.AlwaysResize = ").append(bestDPISetting).append("\n");
            sb.append("\n  To improve:\n");
            
            if (source == DPIScalingStrategy.PatternSource.SIKULI_IDE) {
                sb.append("  - Pattern appears to be from SikuliX IDE (physical pixels)\n");
                sb.append("  - Expected DPI setting for ").append((int)(displayScale * 100))
                  .append("% scaling: ").append((float)(1.0 / displayScale)).append("\n");
            } else {
                sb.append("  - Pattern appears to be from Windows tool (logical pixels)\n");
                sb.append("  - Consider recapturing with SikuliX IDE for consistency\n");
            }
        } else {
            sb.append("❌ Pattern matching needs improvement!\n");
            sb.append("  Best achieved: ").append(String.format("%.1f%%", bestSimilarity * 100))
              .append(" with DPI ").append(bestDPISetting).append("\n");
            sb.append("\n  Troubleshooting steps:\n");
            sb.append("  1. Verify pattern matches what's on screen\n");
            sb.append("  2. Check browser/app zoom is 100%\n");
            sb.append("  3. Consider recapturing pattern\n");
            sb.append("  4. Try pre-scaling pattern to 80% size\n");
        }
        
        return sb.toString();
    }
    
    private static float findMostCommonDPI(List<DiagnosticResult> results) {
        // Simple mode calculation
        float sum = 0;
        for (DiagnosticResult r : results) {
            sum += r.bestDPISetting;
        }
        return sum / results.size();
    }
    
    private static String getImageTypeString(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB: return "RGB (no alpha)";
            case BufferedImage.TYPE_INT_ARGB: return "ARGB (with alpha)";
            case BufferedImage.TYPE_3BYTE_BGR: return "BGR";
            case BufferedImage.TYPE_4BYTE_ABGR: return "ABGR (with alpha)";
            default: return "Type " + type;
        }
    }
    
    /**
     * Main method for standalone diagnostic
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: DPIScalingDiagnostic <pattern-path> [pattern-path2] ...");
            System.out.println("\nThis tool analyzes pattern images and determines optimal DPI settings");
            System.out.println("for pattern matching when Windows display scaling is active.\n");
            DPIScalingStrategy.printDiagnostics();
            return;
        }
        
        List<String> patterns = new ArrayList<>();
        for (String arg : args) {
            patterns.add(arg);
        }
        
        if (patterns.size() == 1) {
            analyzePattern(patterns.get(0));
        } else {
            analyzePatterns(patterns);
        }
    }
}