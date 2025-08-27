package io.github.jspinak.brobot.core.services;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import org.sikuli.script.Finder;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenCV-based implementation of the PatternMatcher interface.
 * 
 * <p>This implementation uses Sikuli's Finder (which wraps OpenCV) for
 * pattern matching operations. It is completely independent of the Find
 * action and other high-level Brobot components, providing pure pattern
 * matching functionality.</p>
 * 
 * <p>Key characteristics:
 * <ul>
 *   <li>No dependencies on Find or Action classes</li>
 *   <li>Thread-safe through stateless operations</li>
 *   <li>Properly manages Finder resources</li>
 *   <li>Handles pattern size validation</li>
 * </ul>
 * </p>
 * 
 * @since 2.0.0
 */
@Component
public class OpenCVPatternMatcher implements PatternMatcher {
    
    @Override
    public List<MatchResult> findPatterns(BufferedImage screen, Pattern pattern, MatchOptions options) {
        // Validate inputs
        if (screen == null || pattern == null || options == null) {
            return new ArrayList<>();
        }
        
        // Check if pattern has valid image data
        if (pattern.getImage() == null || pattern.getImage().isEmpty()) {
            ConsoleReporter.println("[OpenCVPatternMatcher] Pattern has no valid image data");
            return new ArrayList<>();
        }
        
        // Check size constraints
        if (pattern.w() > screen.getWidth() || pattern.h() > screen.getHeight()) {
            ConsoleReporter.println("[OpenCVPatternMatcher] Pattern larger than screen");
            return new ArrayList<>();
        }
        
        // Create Sikuli pattern with similarity threshold
        org.sikuli.script.Pattern sikuliPattern = pattern.sikuli();
        if (Math.abs(sikuliPattern.getSimilar() - options.getSimilarity()) > 0.01) {
            sikuliPattern = sikuliPattern.similar(options.getSimilarity());
        }
        
        // Create Finder and perform search
        Finder finder = null;
        try {
            finder = new Finder(screen);
            
            if (options.isFindAll()) {
                finder.findAll(sikuliPattern);
            } else {
                finder.find(sikuliPattern);
            }
            
            // Extract results
            List<MatchResult> results = new ArrayList<>();
            while (finder.hasNext()) {
                org.sikuli.script.Match sikuliMatch = finder.next();
                results.add(new MatchResult(
                    sikuliMatch.x,
                    sikuliMatch.y,
                    sikuliMatch.w,
                    sikuliMatch.h,
                    sikuliMatch.getScore()
                ));
                
                // Respect max matches limit
                if (!options.isFindAll() || results.size() >= options.getMaxMatches()) {
                    break;
                }
            }
            
            return results;
            
        } finally {
            // Always clean up Finder resources
            if (finder != null) {
                finder.destroy();
            }
        }
    }
    
    @Override
    public List<MatchResult> findPatternsInRegion(BufferedImage screen, Pattern pattern,
                                                  int regionX, int regionY,
                                                  int regionWidth, int regionHeight,
                                                  MatchOptions options) {
        // Validate region bounds
        if (regionX < 0 || regionY < 0 || 
            regionX + regionWidth > screen.getWidth() ||
            regionY + regionHeight > screen.getHeight()) {
            ConsoleReporter.println("[OpenCVPatternMatcher] Invalid region bounds");
            return new ArrayList<>();
        }
        
        // Extract sub-image for the region
        BufferedImage regionImage = screen.getSubimage(regionX, regionY, regionWidth, regionHeight);
        
        // Find patterns in the region
        List<MatchResult> regionMatches = findPatterns(regionImage, pattern, options);
        
        // Adjust coordinates to screen space
        List<MatchResult> adjustedResults = new ArrayList<>();
        for (MatchResult match : regionMatches) {
            adjustedResults.add(new MatchResult(
                match.getX() + regionX,
                match.getY() + regionY,
                match.getWidth(),
                match.getHeight(),
                match.getConfidence()
            ));
        }
        
        return adjustedResults;
    }
    
    @Override
    public boolean supportsPattern(Pattern pattern) {
        // This matcher supports standard image patterns
        // It doesn't support text patterns or special pattern types
        return pattern != null && 
               pattern.getImage() != null && 
               !pattern.getImage().isEmpty();
    }
    
    @Override
    public String getImplementationName() {
        return "OpenCV/Sikuli";
    }
}