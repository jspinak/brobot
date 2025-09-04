package io.github.jspinak.brobot.tools.diagnostics;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests pattern matching across different capture methods and resolutions:
 * 1. SikuliX pattern on normal Brobot capture (1536x864)
 * 2. SikuliX pattern on adjusted 1920x1080 capture
 * 3. Windows pattern on normal Brobot capture (1536x864)
 * 4. Windows pattern on adjusted 1920x1080 capture
 */
public class ComprehensiveCaptureTest {
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final String CLAUDE_PATH = "/home/jspinak/brobot_parent/claude-automator/images/prompt/";
    
    // Test result storage
    static class TestResult {
        String patternName;
        String captureType;
        int captureWidth;
        int captureHeight;
        float dpiSetting;
        double similarity;
        boolean found;
        
        TestResult(String pattern, String capture, int w, int h, float dpi, double sim, boolean found) {
            this.patternName = pattern;
            this.captureType = capture;
            this.captureWidth = w;
            this.captureHeight = h;
            this.dpiSetting = dpi;
            this.similarity = sim;
            this.found = found;
        }
    }
    
    private static List<TestResult> results = new ArrayList<>();
    
    public static void main(String[] args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("COMPREHENSIVE CAPTURE TEST");
        System.out.println("Testing patterns across different capture methods");
        System.out.println("Time: " + dateFormat.format(new Date()));
        System.out.println("=".repeat(80));
        
        Screen screen = new Screen();
        
        // Test patterns
        String[] patterns = {
            "claude-prompt-3.png",     // SikuliX capture
            "claude-prompt-from-windows-snipping.png",    // Windows capture
            "claude-prompt-3-80.png"    // 80% scaled
        };
        
        // First, capture the screen normally and at adjusted resolution
        System.out.println("\n1. CAPTURING SCREENS:");
        System.out.println("-".repeat(70));
        
        // Normal Brobot capture (expected 1536x864)
        Settings.AlwaysResize = 1.0f;
        BufferedImage normalCapture = captureFullScreen(screen, "normal");
        System.out.println("   Normal capture: " + normalCapture.getWidth() + "x" + normalCapture.getHeight());
        
        // Adjusted capture to 1920x1080
        BufferedImage adjustedCapture = resizeImage(normalCapture, 1920, 1080);
        File adjustedFile = saveCapture(adjustedCapture, "adjusted_1920x1080");
        System.out.println("   Adjusted capture: " + adjustedCapture.getWidth() + "x" + adjustedCapture.getHeight());
        
        // Also try capturing with different AlwaysResize settings
        Settings.AlwaysResize = 1.25f;
        BufferedImage capture125 = captureFullScreen(screen, "resize_125");
        System.out.println("   Capture with AlwaysResize=1.25: " + capture125.getWidth() + "x" + capture125.getHeight());
        
        Settings.AlwaysResize = 0.8f;
        BufferedImage capture80 = captureFullScreen(screen, "resize_80");
        System.out.println("   Capture with AlwaysResize=0.8: " + capture80.getWidth() + "x" + capture80.getHeight());
        
        // Now test each pattern
        System.out.println("\n2. TESTING PATTERNS:");
        System.out.println("=" + "=".repeat(79));
        
        for (String patternName : patterns) {
            String patternPath = CLAUDE_PATH + patternName;
            File patternFile = new File(patternPath);
            
            if (!patternFile.exists()) {
                System.out.println("\n❌ Pattern not found: " + patternName);
                continue;
            }
            
            System.out.println("\n📁 TESTING: " + patternName);
            System.out.println("-".repeat(70));
            
            try {
                BufferedImage patternImage = ImageIO.read(patternFile);
                System.out.println("   Pattern dimensions: " + patternImage.getWidth() + "x" + patternImage.getHeight());
                
                // For 1536x864 capture, scale patterns down by 0.8
                // For 1920x1080 capture, use original size
                
                // Test 1: Pattern on normal capture (1536x864) - scale down by 0.8
                System.out.println("\n   TEST 1: On normal Brobot capture (1536x864):");
                System.out.println("   Strategy: Scale patterns down by 0.8 for 1536x864 capture");
                BufferedImage scaledForNormal = resizeImage(patternImage, 
                    (int)(patternImage.getWidth() * 0.8), 
                    (int)(patternImage.getHeight() * 0.8));
                File scaledForNormalFile = saveCapture(scaledForNormal, patternName.replace(".png", "_scaled_for_normal"));
                if (scaledForNormalFile != null) {
                    testPatternOnCapture(screen, scaledForNormalFile.getAbsolutePath(), normalCapture, "Normal", patternName + " (scaled 0.8)");
                }
                // Also test original size for comparison
                testPatternOnCapture(screen, patternPath, normalCapture, "Normal", patternName + " (original)");
                
                // Test 2: Pattern on adjusted 1920x1080 capture - use original size
                System.out.println("\n   TEST 2: On adjusted 1920x1080 capture:");
                System.out.println("   Strategy: Use original pattern size for 1920x1080 capture");
                testPatternOnCapture(screen, patternPath, adjustedCapture, "Adjusted", patternName);
                
                // Test 3: Pattern on capture with AlwaysResize=1.25
                System.out.println("\n   TEST 3: On capture with AlwaysResize=1.25:");
                testPatternOnCapture(screen, patternPath, capture125, "Resize1.25", patternName);
                
                // Test 4: Direct screen search (for comparison)
                System.out.println("\n   TEST 4: Direct screen search:");
                testDirectScreenSearch(screen, patternPath, patternName);
                
            } catch (IOException e) {
                System.err.println("   Error reading pattern: " + e.getMessage());
            }
        }
        
        // Test with resized patterns
        System.out.println("\n3. TESTING WITH RESIZED PATTERNS:");
        System.out.println("=" + "=".repeat(79));
        
        testResizedPatterns(screen, normalCapture, adjustedCapture);
        
        // Test 5 & 6: Capture matches and use them as patterns
        System.out.println("\n4. CAPTURING MATCHES AND REVERSE TESTING:");
        System.out.println("=" + "=".repeat(79));
        System.out.println("  [Skipped - Method not yet implemented]");
        
        // Test 7: Create visual comparison
        System.out.println("\n5. CREATING VISUAL COMPARISON:");
        System.out.println("=" + "=".repeat(79));
        
        createVisualComparison(screen);
        
        // Print summary
        printSummary();
    }
    
    private static void testPatternOnCapture(Screen screen, String patternPath, 
                                            BufferedImage capture, String captureType,
                                            String patternName) {
        // Save the capture as a temporary file to search within it
        File captureFile = saveCapture(capture, "test_capture_" + captureType);
        
        if (captureFile == null) {
            System.out.println("     ❌ Failed to save capture");
            return;
        }
        
        System.out.println("     Searching in " + capture.getWidth() + "x" + capture.getHeight() + " capture:");
        
        // Test with different DPI settings
        float[] dpiSettings = {1.0f};  // Simplified - pattern is already scaled appropriately
        
        for (float dpi : dpiSettings) {
            Settings.AlwaysResize = dpi;
            
            try {
                // Load pattern to check its size
                File patternFile = new File(patternPath);
                if (patternFile.exists()) {
                    BufferedImage patternImg = ImageIO.read(patternFile);
                    System.out.println("     Pattern size: " + patternImg.getWidth() + "x" + patternImg.getHeight());
                }
                
                // Create a Finder to search within the captured image
                Finder finder = new Finder(captureFile.getAbsolutePath());
                Pattern pattern = new Pattern(patternPath).similar(0.3);
                finder.find(pattern);
                
                boolean foundAny = false;
                Match bestMatch = null;
                double bestScore = 0;
                
                while (finder.hasNext()) {
                    Match match = finder.next();
                    double score = match.getScore();
                    
                    if (score > bestScore) {
                        bestMatch = match;
                        bestScore = score;
                    }
                    
                    if (!foundAny) {  // Only report the best match
                        System.out.printf("     Similarity: %.1f%%", score * 100);
                        
                        if (score > 0.90) System.out.println(" ✅ EXCELLENT");
                        else if (score > 0.80) System.out.println(" ✓ Very Good");
                        else if (score > 0.70) System.out.println(" ⚠ Good");
                        else if (score > 0.60) System.out.println(" ⚠ Moderate");
                        else System.out.println(" ❌ Poor");
                        
                        System.out.println("     Match location: " + match.x + "," + match.y);
                        
                        results.add(new TestResult(patternName, captureType, capture.getWidth(), 
                                                 capture.getHeight(), dpi, score, true));
                        foundAny = true;
                    }
                }
                
                // Create visualization if we found a match
                if (bestMatch != null && patternFile.exists()) {
                    BufferedImage patternImg = ImageIO.read(patternFile);
                    createMatchVisualization(capture, patternImg, bestMatch, 
                                           patternName.replace(".png", "") + "_" + captureType);
                }
                
                if (!foundAny) {
                    System.out.println("     No match found");
                    results.add(new TestResult(patternName, captureType, capture.getWidth(), 
                                             capture.getHeight(), dpi, 0.0, false));
                }
                
                finder.destroy();
                
            } catch (Exception e) {
                System.err.println("     Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private static void testDirectScreenSearch(Screen screen, String patternPath, String patternName) {
        float[] dpiSettings = {1.0f, 0.8f, 1.25f, 0.67f};
        
        for (float dpi : dpiSettings) {
            Settings.AlwaysResize = dpi;
            
            try {
                Pattern pattern = new Pattern(patternPath).similar(0.3);
                Match match = screen.exists(pattern, 0.1);
                
                if (match != null) {
                    double score = match.getScore();
                    System.out.printf("     DPI %.2f: %.1f%%", dpi, score * 100);
                    
                    if (score > 0.90) System.out.println(" ✅");
                    else if (score > 0.80) System.out.println(" ✓");
                    else if (score > 0.70) System.out.println(" ⚠");
                    else System.out.println("");
                    
                    results.add(new TestResult(patternName, "DirectScreen", 0, 0, dpi, score, true));
                } else {
                    System.out.printf("     DPI %.2f: No match\n", dpi);
                    results.add(new TestResult(patternName, "DirectScreen", 0, 0, dpi, 0.0, false));
                }
            } catch (Exception e) {
                System.err.printf("     DPI %.2f: Error\n", dpi);
            }
        }
    }
    
    private static void testResizedPatterns(Screen screen, BufferedImage normalCapture, 
                                           BufferedImage adjustedCapture) {
        System.out.println("\nTesting with dynamically resized patterns:");
        
        String testPattern = CLAUDE_PATH + "claude-prompt-3.png";
        File patternFile = new File(testPattern);
        
        if (!patternFile.exists()) {
            System.out.println("   Original pattern not found for resizing");
            return;
        }
        
        try {
            BufferedImage original = ImageIO.read(patternFile);
            
            // Create resized versions
            BufferedImage pattern80 = resizeImage(original, 
                                                 (int)(original.getWidth() * 0.8), 
                                                 (int)(original.getHeight() * 0.8));
            BufferedImage pattern125 = resizeImage(original, 
                                                  (int)(original.getWidth() * 1.25), 
                                                  (int)(original.getHeight() * 1.25));
            
            // Save resized patterns
            File pattern80File = saveCapture(pattern80, "pattern_80_percent");
            File pattern125File = saveCapture(pattern125, "pattern_125_percent");
            
            System.out.println("\n   80% resized pattern (" + pattern80.getWidth() + "x" + pattern80.getHeight() + "):");
            if (pattern80File != null) {
                testPatternOnCapture(screen, pattern80File.getAbsolutePath(), normalCapture, 
                                   "Normal", "Resized80");
            }
            
            System.out.println("\n   125% resized pattern (" + pattern125.getWidth() + "x" + pattern125.getHeight() + "):");
            if (pattern125File != null) {
                testPatternOnCapture(screen, pattern125File.getAbsolutePath(), adjustedCapture, 
                                   "Adjusted", "Resized125");
            }
            
        } catch (IOException e) {
            System.err.println("   Error resizing patterns: " + e.getMessage());
        }
    }
    private static void createMatchVisualization(BufferedImage screenshot, BufferedImage pattern, 
                                                Match match, String outputName) {
        System.out.println("   Creating match visualization for " + outputName);
        
        try {
            // Create a new image that shows the screenshot with the match highlighted
            // and the pattern placed vertically next to it
            int totalWidth = screenshot.getWidth() + pattern.getWidth() + 60; // 60px for spacing and labels
            int totalHeight = Math.max(screenshot.getHeight(), pattern.getHeight() + 100);
            
            BufferedImage visualization = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = visualization.createGraphics();
            
            // Set rendering hints for quality
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // White background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, visualization.getWidth(), visualization.getHeight());
            
            // Draw the screenshot
            g.drawImage(screenshot, 10, 30, null);
            
            // Draw rectangle around match location if match found
            if (match != null) {
                g.setColor(Color.RED);
                g.setStroke(new java.awt.BasicStroke(3));
                g.drawRect(10 + match.x, 30 + match.y, match.w, match.h);
                
                // Draw similarity score
                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                String scoreText = String.format("Match: %.1f%%", match.getScore() * 100);
                g.drawString(scoreText, 10 + match.x, 30 + match.y - 5);
            }
            
            // Draw the pattern on the right
            int patternX = screenshot.getWidth() + 30;
            int patternY = 60;
            g.drawImage(pattern, patternX, patternY, null);
            
            // Draw border around pattern
            g.setColor(Color.BLUE);
            g.setStroke(new java.awt.BasicStroke(2));
            g.drawRect(patternX, patternY, pattern.getWidth(), pattern.getHeight());
            
            // Add labels
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("Screenshot (" + screenshot.getWidth() + "x" + screenshot.getHeight() + ")", 10, 20);
            g.drawString("Pattern (" + pattern.getWidth() + "x" + pattern.getHeight() + ")", patternX, patternY - 10);
            
            g.dispose();
            
            // Save the visualization
            File file = saveCapture(visualization, "match_viz_" + outputName);
            if (file != null) {
                System.out.println("     ✅ Saved: " + file.getName());
            }
            
        } catch (Exception e) {
            System.err.println("     Error creating match visualization: " + e.getMessage());
        }
    }
    
    private static void createVisualComparison(Screen screen) {
        System.out.println("\nCreating side-by-side visual comparison:");
        
        try {
            // Load the patterns
            File sikuliFile = new File(CLAUDE_PATH + "claude-prompt-3.png");
            File windowsFile = new File(CLAUDE_PATH + "claude-prompt-from-windows-snipping.png");
            File scaledFile = new File(CLAUDE_PATH + "claude-prompt-3-80.png");
            
            BufferedImage sikuliImg = sikuliFile.exists() ? ImageIO.read(sikuliFile) : null;
            BufferedImage windowsImg = windowsFile.exists() ? ImageIO.read(windowsFile) : null;
            BufferedImage scaledImg = scaledFile.exists() ? ImageIO.read(scaledFile) : null;
            
            // Try to capture a match from screen
            BufferedImage capturedMatch = null;
            String capturedInfo = "Not found";
            
            // Try to find and capture any pattern
            for (String patternName : new String[]{"claude-prompt-3.png", "claude-prompt-3-80.png"}) {
                String patternPath = CLAUDE_PATH + patternName;
                File f = new File(patternPath);
                if (!f.exists()) continue;
                
                Settings.AlwaysResize = 0.8f; // Use common successful setting
                Pattern pattern = new Pattern(patternPath).similar(0.3);
                Match match = screen.exists(pattern, 0.5);
                
                if (match != null) {
                    Region matchRegion = new Region(match);
                    capturedMatch = screen.capture(matchRegion).getImage();
                    capturedInfo = String.format("Captured %dx%d (%.1f%%)", 
                                                capturedMatch.getWidth(), 
                                                capturedMatch.getHeight(),
                                                match.getScore() * 100);
                    break;
                }
            }
            
            // Create comparison image
            int maxHeight = 200; // Scale all to max 200px height for comparison
            int padding = 20;
            int labelHeight = 30;
            
            // Calculate scaled dimensions
            BufferedImage[] images = {sikuliImg, windowsImg, scaledImg, capturedMatch};
            String[] labels = {
                sikuliImg != null ? String.format("SikuliX Original\n%dx%d", sikuliImg.getWidth(), sikuliImg.getHeight()) : "Not found",
                windowsImg != null ? String.format("Windows Capture\n%dx%d", windowsImg.getWidth(), windowsImg.getHeight()) : "Not found",
                scaledImg != null ? String.format("80%% Scaled\n%dx%d", scaledImg.getWidth(), scaledImg.getHeight()) : "Not found",
                capturedMatch != null ? "Live Capture\n" + capturedInfo : "Not found"
            };
            
            // Calculate total width
            int totalWidth = padding;
            int[] widths = new int[4];
            for (int i = 0; i < images.length; i++) {
                if (images[i] != null) {
                    double scale = (double)maxHeight / images[i].getHeight();
                    widths[i] = (int)(images[i].getWidth() * scale);
                    totalWidth += widths[i] + padding;
                } else {
                    widths[i] = 100; // Placeholder width
                    totalWidth += widths[i] + padding;
                }
            }
            
            // Create comparison image
            BufferedImage comparison = new BufferedImage(totalWidth, maxHeight + labelHeight + padding * 2, 
                                                        BufferedImage.TYPE_INT_RGB);
            Graphics2D g = comparison.createGraphics();
            
            // White background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, comparison.getWidth(), comparison.getHeight());
            
            // Draw each image
            int xOffset = padding;
            for (int i = 0; i < images.length; i++) {
                // Draw label
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                
                String[] lines = labels[i].split("\n");
                for (int j = 0; j < lines.length; j++) {
                    g.drawString(lines[j], xOffset, padding + j * 15);
                }
                
                // Draw image or placeholder
                if (images[i] != null) {
                    double scale = (double)maxHeight / images[i].getHeight();
                    int scaledWidth = (int)(images[i].getWidth() * scale);
                    int scaledHeight = maxHeight;
                    
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(images[i], xOffset, labelHeight + padding, 
                               scaledWidth, scaledHeight, null);
                    
                    // Draw border
                    g.setColor(Color.GRAY);
                    g.drawRect(xOffset, labelHeight + padding, scaledWidth, scaledHeight);
                } else {
                    // Draw placeholder
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(xOffset, labelHeight + padding, widths[i], maxHeight);
                    g.setColor(Color.GRAY);
                    g.drawRect(xOffset, labelHeight + padding, widths[i], maxHeight);
                    g.setColor(Color.DARK_GRAY);
                    g.drawString("Not Available", xOffset + 10, labelHeight + padding + maxHeight/2);
                }
                
                xOffset += widths[i] + padding;
            }
            
            g.dispose();
            
            // Save comparison
            File comparisonFile = saveCapture(comparison, "visual_comparison");
            if (comparisonFile != null) {
                System.out.println("   ✅ Saved visual comparison: " + comparisonFile.getName());
                System.out.println("   Shows side-by-side: SikuliX | Windows | 80% Scaled | Live Capture");
            }
            
            // Print analysis
            System.out.println("\n   VISUAL ANALYSIS:");
            if (sikuliImg != null) {
                System.out.println("   SikuliX pattern: " + sikuliImg.getWidth() + "x" + sikuliImg.getHeight());
            }
            if (windowsImg != null) {
                System.out.println("   Windows pattern: " + windowsImg.getWidth() + "x" + windowsImg.getHeight());
                if (sikuliImg != null) {
                    double ratio = (double)windowsImg.getWidth() / sikuliImg.getWidth();
                    System.out.println("   Windows/SikuliX ratio: " + String.format("%.3f", ratio));
                }
            }
            if (scaledImg != null) {
                System.out.println("   80% scaled: " + scaledImg.getWidth() + "x" + scaledImg.getHeight());
            }
            if (capturedMatch != null) {
                System.out.println("   Live capture: " + capturedInfo);
            }
            
        } catch (Exception e) {
            System.err.println("   Error creating visual comparison: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printSummary() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SUMMARY OF RESULTS:");
        System.out.println("=".repeat(80));
        
        // Find best combinations
        System.out.println("\n🏆 BEST MATCHES (>80% similarity):");
        results.stream()
               .filter(r -> r.similarity > 0.80)
               .sorted((a, b) -> Double.compare(b.similarity, a.similarity))
               .limit(10)
               .forEach(r -> {
                   System.out.printf("   %.1f%% - %s on %s capture (%dx%d) with DPI %.2f\n",
                                   r.similarity * 100, r.patternName, r.captureType,
                                   r.captureWidth, r.captureHeight, r.dpiSetting);
               });
        
        // Analyze by pattern type
        System.out.println("\n📊 ANALYSIS BY PATTERN:");
        
        for (String pattern : new String[]{"claude-prompt-3.png", "claude-prompt-from-windows-snipping.png", "claude-prompt-3-80.png"}) {
            System.out.println("\n   " + pattern + ":");
            
            results.stream()
                   .filter(r -> r.patternName.equals(pattern))
                   .sorted((a, b) -> Double.compare(b.similarity, a.similarity))
                   .limit(3)
                   .forEach(r -> {
                       System.out.printf("     %.1f%% on %s with DPI %.2f\n",
                                       r.similarity * 100, r.captureType, r.dpiSetting);
                   });
        }
        
        System.out.println("\n💡 CONCLUSIONS:");
        
        // Determine which capture method works best
        double normalAvg = results.stream()
            .filter(r -> r.captureType.equals("Normal"))
            .mapToDouble(r -> r.similarity)
            .average().orElse(0);
            
        double adjustedAvg = results.stream()
            .filter(r -> r.captureType.equals("Adjusted"))
            .mapToDouble(r -> r.similarity)
            .average().orElse(0);
            
        System.out.println("   Average on normal capture (1536x864): " + String.format("%.1f%%", normalAvg * 100));
        System.out.println("   Average on adjusted capture (1920x1080): " + String.format("%.1f%%", adjustedAvg * 100));
        
        if (normalAvg > adjustedAvg) {
            System.out.println("\n   ✅ Normal Brobot capture (1536x864) works better");
            System.out.println("   → Use 80% pre-scaled patterns or Settings.AlwaysResize=1.25");
        } else {
            System.out.println("\n   ✅ Adjusted 1920x1080 capture works better");
            System.out.println("   → Consider resizing captures to 1920x1080 before matching");
        }
        
        System.out.println("\n" + "=".repeat(80));
    }
    
    private static BufferedImage captureFullScreen(Screen screen, String label) {
        try {
            ScreenImage screenImage = screen.capture();
            BufferedImage capture = screenImage.getImage();
            saveCapture(capture, "screen_" + label);
            return capture;
        } catch (Exception e) {
            System.err.println("Failed to capture screen: " + e.getMessage());
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
    }
    
    private static BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resized;
    }
    
    private static File saveCapture(BufferedImage image, String prefix) {
        try {
            File debugDir = new File("debug_captures");
            if (!debugDir.exists()) debugDir.mkdirs();
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File file = new File(debugDir, prefix + "_" + timestamp + ".png");
            ImageIO.write(image, "png", file);
            return file;
        } catch (IOException e) {
            System.err.println("Failed to save capture: " + e.getMessage());
            return null;
        }
    }
}