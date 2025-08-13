package io.github.jspinak.brobot.finder;

import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.ScreenImage;
import org.sikuli.script.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * A Finder wrapper that automatically corrects match dimensions on Windows with DPI scaling.
 * 
 * Problem: On Windows with DPI scaling (e.g., 125%), SikuliX's Finder returns matches
 * in logical coordinates even when the search is performed on physical resolution images.
 * 
 * Solution: This wrapper detects dimension mismatches and scales match coordinates
 * to ensure match dimensions equal search image dimensions.
 */
public class ScaledFinder extends Finder {
    
    private final Finder delegate;
    private final Pattern searchPattern;
    private float scaleFactor = 1.0f;
    private boolean needsScaling = false;
    
    public ScaledFinder(ScreenImage screenImage, Pattern pattern) {
        super(screenImage);
        this.delegate = new Finder(screenImage);
        this.searchPattern = pattern;
        detectScaling();
    }
    
    public ScaledFinder(BufferedImage image, Pattern pattern) {
        super(image);
        this.delegate = new Finder(image);
        this.searchPattern = pattern;
        detectScaling();
    }
    
    private void detectScaling() {
        // Check if we're on Windows
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("windows")) {
            return; // No scaling needed on non-Windows
        }
        
        try {
            // Get physical vs logical resolution
            GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
            
            DisplayMode mode = device.getDisplayMode();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            
            int physicalWidth = mode.getWidth();
            int logicalWidth = (int) screenSize.getWidth();
            
            if (physicalWidth != logicalWidth) {
                // Calculate scale factor
                // On 125% DPI: physical=1920, logical=1536, scale=0.8
                scaleFactor = (float) logicalWidth / physicalWidth;
                needsScaling = true;
                
                System.out.println("ScaledFinder: Detected Windows DPI scaling");
                System.out.println("  Scale factor: " + scaleFactor);
                System.out.println("  Will correct match dimensions automatically");
            }
        } catch (Exception e) {
            // If detection fails, we'll detect scaling from match results
        }
    }
    
    @Override
    public boolean find(Pattern pattern) {
        boolean found = delegate.find(pattern);
        if (found && !needsScaling) {
            // Check if match dimensions are wrong
            checkAndDetectScalingFromMatch(pattern);
        }
        return found;
    }
    
    @Override
    public boolean findAll(Pattern pattern) {
        return delegate.findAll(pattern);
    }
    
    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }
    
    @Override
    public Match next() {
        Match match = delegate.next();
        if (needsScaling) {
            return scaleMatch(match);
        }
        return match;
    }
    
    private void checkAndDetectScalingFromMatch(Pattern pattern) {
        if (!delegate.hasNext()) {
            return;
        }
        
        // Peek at the first match to check dimensions
        Match firstMatch = delegate.next();
        
        try {
            // Get pattern dimensions
            BufferedImage patternImage = getPatternImage(pattern);
            if (patternImage != null) {
                int patternWidth = patternImage.getWidth();
                int patternHeight = patternImage.getHeight();
                Rectangle matchRect = firstMatch.getRect();
                
                // Check if dimensions match
                if (matchRect.width != patternWidth || matchRect.height != patternHeight) {
                    // Calculate scale factor from dimension mismatch
                    float scaleX = (float) matchRect.width / patternWidth;
                    float scaleY = (float) matchRect.height / patternHeight;
                    
                    if (Math.abs(scaleX - scaleY) < 0.01) { // They should be the same
                        scaleFactor = scaleX;
                        needsScaling = true;
                        
                        System.out.println("ScaledFinder: Detected dimension mismatch");
                        System.out.println("  Pattern: " + patternWidth + "x" + patternHeight);
                        System.out.println("  Match: " + matchRect.width + "x" + matchRect.height);
                        System.out.println("  Auto-detected scale: " + scaleFactor);
                    }
                }
            }
        } catch (Exception e) {
            // If we can't detect, proceed without scaling
        }
        
        // Reset the finder to include the first match
        delegate.find(pattern);
    }
    
    private BufferedImage getPatternImage(Pattern pattern) {
        try {
            // Try to get the image from the pattern
            if (pattern.getImage() != null) {
                return pattern.getImage().get();
            }
            
            // Try to load from file if pattern has a filename
            if (pattern.getFilename() != null && !pattern.getFilename().isEmpty()) {
                return ImageIO.read(new File(pattern.getFilename()));
            }
        } catch (Exception e) {
            // Unable to get pattern image
        }
        return null;
    }
    
    private Match scaleMatch(Match original) {
        Rectangle rect = original.getRect();
        
        // Scale the match dimensions UP to physical resolution
        // If scale is 0.8 (125% DPI), we multiply by 1/0.8 = 1.25
        float inverseScale = 1.0f / scaleFactor;
        
        int scaledX = Math.round(rect.x * inverseScale);
        int scaledY = Math.round(rect.y * inverseScale);
        int scaledWidth = Math.round(rect.width * inverseScale);
        int scaledHeight = Math.round(rect.height * inverseScale);
        
        // Create a new match with corrected dimensions
        Rectangle scaledRect = new Rectangle(scaledX, scaledY, scaledWidth, scaledHeight);
        
        // Create new match with scaled rectangle
        // Note: This is a simplified version - in production you'd preserve all match properties
        return new CorrectedMatch(original, scaledRect);
    }
    
    /**
     * A Match wrapper that reports corrected dimensions.
     */
    private static class CorrectedMatch extends Match {
        private final Match original;
        private final Rectangle correctedRect;
        
        public CorrectedMatch(Match original, Rectangle correctedRect) {
            super(correctedRect, original.getScore());
            this.original = original;
            this.correctedRect = correctedRect;
        }
        
        @Override
        public Rectangle getRect() {
            return correctedRect;
        }
        
        @Override
        public int getX() {
            return correctedRect.x;
        }
        
        @Override
        public int getY() {
            return correctedRect.y;
        }
        
        @Override
        public int getW() {
            return correctedRect.width;
        }
        
        @Override
        public int getH() {
            return correctedRect.height;
        }
        
        @Override
        public double getScore() {
            return original.getScore();
        }
    }
}