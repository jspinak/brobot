package io.github.jspinak.brobot.util.image.capture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Normalizes UI scale differences between pattern and scene images.
 * 
 * <p>This class addresses the issue where UI elements appear at different sizes
 * within images of the same pixel dimensions. This commonly occurs when:</p>
 * <ul>
 *   <li>Browser zoom levels change between captures</li>
 *   <li>Operating system UI scaling changes</li>
 *   <li>Application DPI settings change</li>
 * </ul>
 * 
 * <p>The normalizer detects the apparent UI scale in both pattern and scene,
 * then rescales one to match the other for successful pattern matching.</p>
 * 
 * @since 1.1.0
 */
@Slf4j
@Component
public class UIScaleNormalizer {
    
    /**
     * Detects the UI scale by measuring bright UI elements.
     * 
     * @param image The image to analyze
     * @return The detected UI element size as a scale factor
     */
    public UIScale detectUIScale(BufferedImage image) {
        if (image == null) {
            return new UIScale(1.0, 0, 0);
        }
        
        // Find the brightest continuous region (likely UI element)
        int minX = image.getWidth(), maxX = 0;
        int minY = image.getHeight(), maxY = 0;
        boolean foundBright = false;
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int brightness = (r + g + b) / 3;
                
                // Threshold for bright UI elements
                if (brightness > 100) {
                    foundBright = true;
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        
        if (!foundBright) {
            log.debug("No bright UI elements found for scale detection");
            return new UIScale(1.0, 0, 0);
        }
        
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        
        log.debug("Detected UI element bounds: {}x{} at ({},{})", 
            width, height, minX, minY);
        
        // Use the average dimension as a scale indicator
        double avgSize = (width + height) / 2.0;
        
        return new UIScale(avgSize / 50.0, width, height); // Normalize to ~50 pixel baseline
    }
    
    /**
     * Normalizes a pattern image to match the UI scale of a scene.
     * 
     * @param pattern The pattern image to normalize
     * @param patternScale The detected scale of the pattern
     * @param sceneScale The detected scale of the scene
     * @return The normalized pattern image
     */
    public BufferedImage normalizePattern(BufferedImage pattern, 
                                         UIScale patternScale, 
                                         UIScale sceneScale) {
        if (pattern == null || patternScale == null || sceneScale == null) {
            return pattern;
        }
        
        // Calculate the scale factor needed
        double scaleFactor = sceneScale.getScale() / patternScale.getScale();
        
        // If scale is close to 1.0, no normalization needed
        if (Math.abs(scaleFactor - 1.0) < 0.05) {
            log.debug("UI scales are similar (factor: {}), no normalization needed", scaleFactor);
            return pattern;
        }
        
        log.info("Normalizing pattern with scale factor: {} (pattern scale: {}, scene scale: {})",
            scaleFactor, patternScale.getScale(), sceneScale.getScale());
        
        // Calculate new dimensions
        int newWidth = (int)(pattern.getWidth() * scaleFactor);
        int newHeight = (int)(pattern.getHeight() * scaleFactor);
        
        // Ensure we don't create an image larger than the original
        // (patterns should not be larger than scenes)
        if (scaleFactor > 1.0) {
            log.warn("Pattern would be scaled up by {}x - keeping original size", scaleFactor);
            return pattern;
        }
        
        return scaleImage(pattern, newWidth, newHeight);
    }
    
    /**
     * Scales an image to the specified dimensions.
     * 
     * @param source The source image
     * @param targetWidth Target width
     * @param targetHeight Target height
     * @return The scaled image
     */
    private BufferedImage scaleImage(BufferedImage source, int targetWidth, int targetHeight) {
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, 
            source.getType() == 0 ? BufferedImage.TYPE_INT_RGB : source.getType());
        
        Graphics2D g = scaled.createGraphics();
        
        // Use high-quality scaling
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, 
                          RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                          RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Scale the image
        g.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        
        log.debug("Scaled image from {}x{} to {}x{}",
            source.getWidth(), source.getHeight(), targetWidth, targetHeight);
        
        return scaled;
    }
    
    /**
     * Attempts to match a pattern in a scene with automatic UI scale normalization.
     * 
     * @param pattern The pattern to find
     * @param scene The scene to search in
     * @return The normalized pattern that should be used for matching
     */
    public BufferedImage prepareForMatching(BufferedImage pattern, BufferedImage scene) {
        // Detect UI scales
        UIScale patternScale = detectUIScale(pattern);
        UIScale sceneScale = detectUIScale(scene);
        
        log.debug("Pattern UI scale: {}, Scene UI scale: {}", 
            patternScale.getScale(), sceneScale.getScale());
        
        // Check if normalization is needed
        double scaleRatio = sceneScale.getScale() / patternScale.getScale();
        
        if (Math.abs(scaleRatio - 1.0) > 0.1) {
            log.info("UI scale mismatch detected (ratio: {}), normalizing pattern", scaleRatio);
            return normalizePattern(pattern, patternScale, sceneScale);
        }
        
        return pattern;
    }
    
    /**
     * Container for UI scale information.
     */
    public static class UIScale {
        private final double scale;
        private final int elementWidth;
        private final int elementHeight;
        
        public UIScale(double scale, int elementWidth, int elementHeight) {
            this.scale = scale;
            this.elementWidth = elementWidth;
            this.elementHeight = elementHeight;
        }
        
        public double getScale() {
            return scale;
        }
        
        public int getElementWidth() {
            return elementWidth;
        }
        
        public int getElementHeight() {
            return elementHeight;
        }
        
        @Override
        public String toString() {
            return String.format("UIScale[scale=%.2f, element=%dx%d]", 
                scale, elementWidth, elementHeight);
        }
    }
}