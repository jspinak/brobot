package io.github.jspinak.brobot.tools.diagnostics;

import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import lombok.extern.slf4j.Slf4j;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Diagnostic tool to compare pattern matching between SikuliX and Brobot.
 * This helps identify why patterns match at 0.99 similarity in SikuliX IDE
 * but only 50-69% in Brobot despite correct DPI scaling.
 */
@Slf4j
@Component
public class SikuliXBrobotComparisonDiagnostic {

    /**
     * Comprehensive comparison of pattern matching between SikuliX and Brobot
     */
    public void comparePatternMatching(StateImage stateImage, String testName) {
        log.info("╔════════════════════════════════════════════════════════════════════╗");
        log.info("║         SIKULIX vs BROBOT PATTERN MATCHING COMPARISON              ║");
        log.info("║                    Test: {}                                        ", testName);
        log.info("╚════════════════════════════════════════════════════════════════════╝");
        
        // Log current settings
        logCurrentSettings();
        
        // Get patterns from StateImage
        for (var pattern : stateImage.getPatterns()) {
            if (pattern.getImage() != null && pattern.getImage().getBufferedImage() != null) {
                String patternName = pattern.getName();
                BufferedImage brobotImage = pattern.getImage().getBufferedImage();
                
                log.info("\n═══════════════════════════════════════════════════════════════════");
                log.info("Testing pattern: {}", patternName);
                log.info("═══════════════════════════════════════════════════════════════════");
                
                // Compare loading methods
                compareLoadingMethods(pattern.getImage(), patternName);
                
                // Test with SikuliX directly
                testWithPureSikuliX(brobotImage, patternName);
                
                // Test with different AlwaysResize values
                testScalingVariations(brobotImage, patternName);
                
                // Analyze pixel differences
                analyzePixelDifferences(pattern.getImage(), patternName);
            }
        }
        
        // Provide recommendations
        provideRecommendations();
    }
    
    private void logCurrentSettings() {
        log.info("\n=== CURRENT SIKULIX SETTINGS ===");
        log.info("Settings.AlwaysResize: {}", Settings.AlwaysResize);
        log.info("Settings.MinSimilarity: {}", Settings.MinSimilarity);
        log.info("Settings.CheckLastSeen: {}", Settings.CheckLastSeen);
        log.info("Settings.AutoWaitTimeout: {}", Settings.AutoWaitTimeout);
    }
    
    private void compareLoadingMethods(Image brobotImage, String patternName) {
        log.info("\n>>> Comparing Image Loading Methods <<<");
        
        try {
            BufferedImage brobotBuffered = brobotImage.getBufferedImage();
            log.info("Brobot BufferedImage:");
            log.info("  Type: {} ({})", brobotBuffered.getType(), getImageTypeName(brobotBuffered.getType()));
            log.info("  Dimensions: {}x{}", brobotBuffered.getWidth(), brobotBuffered.getHeight());
            log.info("  ColorModel: {}", brobotBuffered.getColorModel().getClass().getSimpleName());
            log.info("  Has Alpha: {}", brobotBuffered.getColorModel().hasAlpha());
            
            // Convert to SikuliX Image and back
            org.sikuli.script.Image sikuliImage = brobotImage.sikuli();
            BufferedImage sikuliBuffered = sikuliImage.get();
            
            log.info("After SikuliX conversion:");
            log.info("  Type: {} ({})", sikuliBuffered.getType(), getImageTypeName(sikuliBuffered.getType()));
            log.info("  Dimensions: {}x{}", sikuliBuffered.getWidth(), sikuliBuffered.getHeight());
            
            // Check if pixels are identical
            boolean identical = comparePixels(brobotBuffered, sikuliBuffered);
            log.info("  Pixels identical: {}", identical);
            
        } catch (Exception e) {
            log.error("Error comparing loading methods: ", e);
        }
    }
    
    private void testWithPureSikuliX(BufferedImage image, String patternName) {
        log.info("\n>>> Testing with Pure SikuliX <<<");
        
        try {
            Screen screen = new Screen();
            
            // Test at different similarity levels
            double[] similarities = {0.99, 0.95, 0.90, 0.85, 0.80, 0.70, 0.60, 0.50};
            
            for (double similarity : similarities) {
                Pattern pattern = new Pattern(image).similar(similarity);
                Match match = screen.exists(pattern, 0.1);
                
                if (match != null) {
                    log.info("  ✓ Found at similarity {}: score = {}", similarity, match.getScore());
                    break;
                } else if (similarity == 0.50) {
                    log.info("  ✗ Not found even at similarity 0.50");
                }
            }
            
            // Test with Finder for more detailed analysis
            testWithFinder(screen, image, patternName);
            
        } catch (Exception e) {
            log.error("Error in pure SikuliX test: ", e);
        }
    }
    
    private void testWithFinder(Screen screen, BufferedImage patternImage, String patternName) {
        log.info("\n>>> Using Finder for detailed analysis <<<");
        
        try {
            // Capture screen
            ScreenImage screenCapture = screen.capture();
            BufferedImage screenImage = screenCapture.getImage();
            
            log.info("Screen capture:");
            log.info("  Type: {} ({})", screenImage.getType(), getImageTypeName(screenImage.getType()));
            log.info("  Dimensions: {}x{}", screenImage.getWidth(), screenImage.getHeight());
            
            // Create Finder
            Finder finder = new Finder(screenImage);
            
            // Test at high similarity
            Pattern pattern = new Pattern(patternImage).similar(0.99);
            finder.find(pattern);
            
            List<Match> matches = new ArrayList<>();
            while (finder.hasNext()) {
                matches.add(finder.next());
            }
            
            if (!matches.isEmpty()) {
                log.info("  ✓ Finder found {} matches at 0.99 similarity", matches.size());
                for (Match m : matches) {
                    log.info("    Match: score={}, location=({},{})", m.getScore(), m.x, m.y);
                }
            } else {
                log.info("  ✗ Finder found no matches at 0.99 similarity");
                
                // Try lower similarities
                finder.destroy();
                finder = new Finder(screenImage);
                pattern = new Pattern(patternImage).similar(0.50);
                finder.find(pattern);
                
                int count = 0;
                double maxScore = 0;
                while (finder.hasNext()) {
                    Match m = finder.next();
                    maxScore = Math.max(maxScore, m.getScore());
                    count++;
                }
                
                if (count > 0) {
                    log.info("  At 0.50 similarity: {} matches, max score = {}", count, maxScore);
                }
            }
            
            finder.destroy();
            
        } catch (Exception e) {
            log.error("Error in Finder test: ", e);
        }
    }
    
    private void testScalingVariations(BufferedImage image, String patternName) {
        log.info("\n>>> Testing Scaling Variations <<<");
        
        float originalResize = Settings.AlwaysResize;
        Screen screen = new Screen();
        
        float[] testValues = {0f, 0.5f, 0.67f, 0.8f, 1.0f, 1.25f, 1.5f};
        
        for (float resize : testValues) {
            Settings.AlwaysResize = resize;
            
            Pattern pattern = new Pattern(image).similar(0.70);
            Match match = screen.exists(pattern, 0.1);
            
            if (match != null) {
                log.info("  ✓ AlwaysResize={}: Found with score {}", resize, match.getScore());
            } else {
                log.info("  ✗ AlwaysResize={}: Not found", resize);
            }
        }
        
        Settings.AlwaysResize = originalResize;
        log.info("Restored AlwaysResize to: {}", originalResize);
    }
    
    private void analyzePixelDifferences(Image brobotImage, String patternName) {
        log.info("\n>>> Analyzing Pixel Differences <<<");
        
        try {
            BufferedImage original = brobotImage.getBufferedImage();
            
            // Test different type conversions
            BufferedImage[] conversions = {
                convertToType(original, BufferedImage.TYPE_INT_RGB),
                convertToType(original, BufferedImage.TYPE_INT_ARGB),
                convertToType(original, BufferedImage.TYPE_3BYTE_BGR),
                convertToType(original, BufferedImage.TYPE_4BYTE_ABGR)
            };
            
            String[] typeNames = {"INT_RGB", "INT_ARGB", "3BYTE_BGR", "4BYTE_ABGR"};
            
            for (int i = 0; i < conversions.length; i++) {
                BufferedImage converted = conversions[i];
                boolean identical = comparePixels(original, converted);
                
                if (!identical) {
                    log.info("  Conversion to {} changes pixels!", typeNames[i]);
                    showPixelDifferences(original, converted, 5);
                } else {
                    log.info("  Conversion to {} preserves pixels", typeNames[i]);
                }
            }
            
        } catch (Exception e) {
            log.error("Error analyzing pixel differences: ", e);
        }
    }
    
    private BufferedImage convertToType(BufferedImage src, int targetType) {
        if (src.getType() == targetType) {
            return src;
        }
        
        BufferedImage converted = new BufferedImage(
            src.getWidth(), 
            src.getHeight(), 
            targetType
        );
        
        converted.getGraphics().drawImage(src, 0, 0, null);
        return converted;
    }
    
    private boolean comparePixels(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            return false;
        }
        
        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private void showPixelDifferences(BufferedImage img1, BufferedImage img2, int maxSamples) {
        int differences = 0;
        int samples = 0;
        
        for (int y = 0; y < img1.getHeight() && samples < maxSamples; y++) {
            for (int x = 0; x < img1.getWidth() && samples < maxSamples; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                
                if (rgb1 != rgb2) {
                    if (differences < 3) { // Show first 3 differences
                        log.info("    Pixel ({},{}): {} vs {}", x, y, 
                            String.format("0x%08X", rgb1),
                            String.format("0x%08X", rgb2));
                    }
                    differences++;
                    samples++;
                }
            }
        }
        
        if (differences > 3) {
            log.info("    ... and {} more pixel differences", differences - 3);
        }
    }
    
    private String getImageTypeName(int type) {
        switch(type) {
            case BufferedImage.TYPE_INT_RGB: return "TYPE_INT_RGB";
            case BufferedImage.TYPE_INT_ARGB: return "TYPE_INT_ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE: return "TYPE_INT_ARGB_PRE";
            case BufferedImage.TYPE_INT_BGR: return "TYPE_INT_BGR";
            case BufferedImage.TYPE_3BYTE_BGR: return "TYPE_3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR: return "TYPE_4BYTE_ABGR";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "TYPE_4BYTE_ABGR_PRE";
            case BufferedImage.TYPE_BYTE_GRAY: return "TYPE_BYTE_GRAY";
            case BufferedImage.TYPE_BYTE_BINARY: return "TYPE_BYTE_BINARY";
            case BufferedImage.TYPE_BYTE_INDEXED: return "TYPE_BYTE_INDEXED";
            case BufferedImage.TYPE_USHORT_GRAY: return "TYPE_USHORT_GRAY";
            case BufferedImage.TYPE_USHORT_565_RGB: return "TYPE_USHORT_565_RGB";
            case BufferedImage.TYPE_USHORT_555_RGB: return "TYPE_USHORT_555_RGB";
            case BufferedImage.TYPE_CUSTOM: return "TYPE_CUSTOM";
            default: return "Unknown type " + type;
        }
    }
    
    private void provideRecommendations() {
        log.info("\n═══════════════════════════════════════════════════════════════════");
        log.info("                         RECOMMENDATIONS");
        log.info("═══════════════════════════════════════════════════════════════════");
        
        log.info("\n1. IMAGE TYPE MISMATCH:");
        log.info("   If patterns show Type6 (4BYTE_ABGR) vs Type1 (INT_RGB):");
        log.info("   - Patterns may have been captured with alpha channel");
        log.info("   - Screen captures might not include alpha");
        log.info("   - Consider re-capturing patterns without alpha channel");
        
        log.info("\n2. SCALING FACTOR:");
        log.info("   If patterns only match with AlwaysResize=0:");
        log.info("   - Patterns were captured at a different DPI");
        log.info("   - Try Settings.AlwaysResize = 0 to disable scaling");
        
        log.info("\n3. PIXEL FORMAT:");
        log.info("   If pixel values differ after conversion:");
        log.info("   - Color space conversion may be lossy");
        log.info("   - Ensure patterns and screen use same color profile");
        
        log.info("\n4. SIMILARITY CALCULATION:");
        log.info("   If SikuliX Finder shows different scores than screen.exists:");
        log.info("   - Different similarity algorithms may be in use");
        log.info("   - Consider using Finder directly for critical matches");
        
        log.info("\n5. IMMEDIATE WORKAROUND:");
        log.info("   To match SikuliX IDE behavior exactly:");
        log.info("   - Set Settings.AlwaysResize = 1.0");
        log.info("   - Use similarity threshold of 0.70 instead of 0.99");
        log.info("   - Load patterns with Pattern class directly");
    }
}