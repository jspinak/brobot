package io.github.jspinak.brobot.action.internal.find.pattern;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.Finder;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Adjusts pattern scale to match the UI scale of the scene.
 * 
 * <p>This addresses the issue where UI elements appear at different sizes
 * due to browser zoom, DPI settings, or display scaling changes between
 * when a pattern was captured and when matching occurs.</p>
 * 
 * <p>The adjuster tries multiple scales to find the best match, similar to
 * scale-invariant feature matching but using template matching at different scales.</p>
 * 
 * @since 1.1.0
 */
@Slf4j
@Component
public class PatternScaleAdjuster {
    
    // Scale factors to try (from 50% to 200% in steps)
    private static final double[] SCALE_FACTORS = {
        0.5, 0.6, 0.7, 0.8, 0.9, 
        1.0,  // Original size
        1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0
    };
    
    // Minimum similarity to consider a match valid
    private static final double MIN_SIMILARITY = 0.7;
    
    /**
     * Finds the best scale for a pattern to match in a scene.
     * 
     * @param pattern The pattern to find
     * @param scene The scene to search in
     * @return The best scale factor, or 1.0 if no better scale found
     */
    public double findBestScale(Pattern pattern, Scene scene) {
        if (pattern == null || scene == null) {
            return 1.0;
        }
        
        BufferedImage patternImg = pattern.getBImage();
        BufferedImage sceneImg = scene.getPattern().getBImage();
        
        if (patternImg == null || sceneImg == null) {
            return 1.0;
        }
        
        double bestScale = 1.0;
        double bestScore = 0.0;
        
        log.debug("Testing {} scale factors for pattern '{}'", SCALE_FACTORS.length, pattern.getName());
        
        for (double scale : SCALE_FACTORS) {
            // Skip scales that would make pattern larger than scene
            int scaledWidth = (int)(patternImg.getWidth() * scale);
            int scaledHeight = (int)(patternImg.getHeight() * scale);
            
            if (scaledWidth > sceneImg.getWidth() || scaledHeight > sceneImg.getHeight()) {
                continue;
            }
            
            // Try matching at this scale
            double score = tryMatchAtScale(patternImg, sceneImg, scale);
            
            if (score > bestScore) {
                bestScore = score;
                bestScale = scale;
                log.debug("Better match found at scale {}: score {}", scale, score);
            }
        }
        
        if (Math.abs(bestScale - 1.0) > 0.01) {
            log.info("Pattern '{}' matches better at scale {} (score: {})", 
                pattern.getName(), bestScale, bestScore);
        }
        
        return bestScale;
    }
    
    /**
     * Tries to match a pattern at a specific scale.
     * 
     * @param patternImg The pattern image
     * @param sceneImg The scene image
     * @param scale The scale factor to apply
     * @return The best similarity score at this scale
     */
    private double tryMatchAtScale(BufferedImage patternImg, BufferedImage sceneImg, double scale) {
        // Scale the pattern
        BufferedImage scaledPattern = scaleImage(patternImg, scale);
        
        // Try to find it in the scene
        try {
            Finder finder = new Finder(sceneImg);
            org.sikuli.script.Pattern sikuliPattern = new org.sikuli.script.Pattern(scaledPattern);
            sikuliPattern = sikuliPattern.similar(0.5); // Lower threshold for testing
            
            finder.findAll(sikuliPattern);
            
            double bestScore = 0.0;
            while (finder.hasNext()) {
                org.sikuli.script.Match match = finder.next();
                if (match.getScore() > bestScore) {
                    bestScore = match.getScore();
                }
            }
            
            finder.destroy();
            return bestScore;
            
        } catch (Exception e) {
            log.debug("Error testing scale {}: {}", scale, e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Creates a scaled version of a pattern that should match better in the scene.
     * 
     * @param pattern The pattern to scale
     * @param scene The scene to match against
     * @return A new pattern scaled to match the scene's UI scale, or the original if no scaling needed
     */
    public Pattern createScaledPattern(Pattern pattern, Scene scene) {
        double bestScale = findBestScale(pattern, scene);
        
        if (Math.abs(bestScale - 1.0) < 0.01) {
            // No scaling needed
            return pattern;
        }
        
        BufferedImage originalImg = pattern.getBImage();
        if (originalImg == null) {
            return pattern;
        }
        
        // Create scaled image
        BufferedImage scaledImg = scaleImage(originalImg, bestScale);
        
        // Create new pattern with scaled image
        Pattern scaledPattern = new Pattern(scaledImg);
        scaledPattern.setName(pattern.getName() + "_scaled_" + String.format("%.1f", bestScale));
        
        // Copy other properties
        scaledPattern.setFixed(pattern.isFixed());
        scaledPattern.setSearchRegions(pattern.getSearchRegions());
        
        log.info("Created scaled pattern '{}' at {}x", scaledPattern.getName(), bestScale);
        
        return scaledPattern;
    }
    
    /**
     * Scales an image by the given factor.
     * 
     * @param source The source image
     * @param scale The scale factor
     * @return The scaled image
     */
    private BufferedImage scaleImage(BufferedImage source, double scale) {
        if (source == null) {
            return null;
        }
        
        if (Math.abs(scale - 1.0) < 0.01) {
            return source;
        }
        
        int newWidth = Math.max(1, (int)(source.getWidth() * scale));
        int newHeight = Math.max(1, (int)(source.getHeight() * scale));
        
        BufferedImage scaled = new BufferedImage(newWidth, newHeight, 
            source.getType() == 0 ? BufferedImage.TYPE_INT_RGB : source.getType());
        
        Graphics2D g = scaled.createGraphics();
        
        // High quality scaling
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, 
                          RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                          RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.drawImage(source, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        return scaled;
    }
    
    /**
     * Detects if there's a UI scale mismatch between pattern and scene.
     * 
     * @param pattern The pattern to check
     * @param scene The scene to check against
     * @return true if there appears to be a scale mismatch
     */
    public boolean hasScaleMismatch(Pattern pattern, Scene scene) {
        // Quick test at original scale
        double originalScore = tryMatchAtScale(pattern.getBImage(), scene.getPattern().getBImage(), 1.0);
        
        if (originalScore >= MIN_SIMILARITY) {
            // Good match at original scale
            return false;
        }
        
        // Try a few other scales to see if they work better
        for (double scale : new double[]{0.8, 1.2, 1.5}) {
            double score = tryMatchAtScale(pattern.getBImage(), scene.getPattern().getBImage(), scale);
            if (score > originalScore + 0.2) {
                // Significantly better at different scale
                return true;
            }
        }
        
        return false;
    }
}