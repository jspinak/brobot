package io.github.jspinak.brobot.core.services;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure JavaCV/OpenCV implementation of pattern matching.
 * 
 * This implementation bypasses SikuliX entirely and uses JavaCV's OpenCV bindings
 * directly for template matching. It provides the same functionality as SikuliX's
 * Finder but with direct control and potentially better performance.
 * 
 * Benefits over SikuliX:
 * - Direct OpenCV access (no abstraction overhead)
 * - Smaller memory footprint
 * - Better control over matching algorithms
 * - Consistent with JavaCV FFmpeg capture
 * 
 * @since 2.0.0
 */
@Component
public class JavaCVPatternMatcher implements PatternMatcher {
    
    // OpenCV template matching methods
    private static final int TM_SQDIFF = opencv_imgproc.TM_SQDIFF;
    private static final int TM_SQDIFF_NORMED = opencv_imgproc.TM_SQDIFF_NORMED;
    private static final int TM_CCORR = opencv_imgproc.TM_CCORR;
    private static final int TM_CCORR_NORMED = opencv_imgproc.TM_CCORR_NORMED;
    private static final int TM_CCOEFF = opencv_imgproc.TM_CCOEFF;
    private static final int TM_CCOEFF_NORMED = opencv_imgproc.TM_CCOEFF_NORMED;
    
    @Override
    public List<PatternMatcher.MatchResult> findPatterns(BufferedImage screen, io.github.jspinak.brobot.model.element.Pattern pattern, 
                                          PatternMatcher.MatchOptions options) {
        // Validate inputs
        if (screen == null || pattern == null || options == null) {
            return new ArrayList<>();
        }
        
        // Check pattern has valid image
        BufferedImage patternImage = pattern.getBImage();
        if (patternImage == null) {
            System.err.println("[JavaCVPatternMatcher] Pattern has no image data");
            return new ArrayList<>();
        }
        
        // Check size constraints
        if (patternImage.getWidth() > screen.getWidth() || 
            patternImage.getHeight() > screen.getHeight()) {
            System.err.println("[JavaCVPatternMatcher] Pattern larger than screen");
            return new ArrayList<>();
        }
        
        // Convert BufferedImages to OpenCV Mats
        Mat screenMat = bufferedImageToMat(screen);
        Mat patternMat = bufferedImageToMat(patternImage);
        
        try {
            // Perform template matching
            return performTemplateMatching(screenMat, patternMat, options);
            
        } finally {
            // Clean up native memory
            screenMat.release();
            patternMat.release();
        }
    }
    
    @Override
    public List<PatternMatcher.MatchResult> findPatternsInRegion(BufferedImage screen, 
                                                  io.github.jspinak.brobot.model.element.Pattern pattern,
                                                  int regionX, int regionY, 
                                                  int regionWidth, int regionHeight,
                                                  PatternMatcher.MatchOptions options) {
        // Validate region bounds
        if (regionX < 0 || regionY < 0 || 
            regionX + regionWidth > screen.getWidth() ||
            regionY + regionHeight > screen.getHeight()) {
            System.err.println("[JavaCVPatternMatcher] Invalid region bounds");
            return new ArrayList<>();
        }
        
        // Crop screen to region
        BufferedImage regionImage = screen.getSubimage(regionX, regionY, regionWidth, regionHeight);
        
        // Find patterns in region
        List<PatternMatcher.MatchResult> results = findPatterns(regionImage, pattern, options);
        
        // Adjust coordinates to screen space  
        List<PatternMatcher.MatchResult> adjustedResults = new ArrayList<>();
        for (PatternMatcher.MatchResult result : results) {
            adjustedResults.add(new PatternMatcher.MatchResult(
                result.getX() + regionX,
                result.getY() + regionY,
                result.getWidth(),
                result.getHeight(),
                result.getConfidence()
            ));
        }
        results = adjustedResults;
        
        return results;
    }
    
    /**
     * Performs OpenCV template matching.
     */
    private List<PatternMatcher.MatchResult> performTemplateMatching(Mat screen, Mat pattern, PatternMatcher.MatchOptions options) {
        // Create result matrix
        int resultWidth = screen.cols() - pattern.cols() + 1;
        int resultHeight = screen.rows() - pattern.rows() + 1;
        Mat result = new Mat(resultHeight, resultWidth, opencv_core.CV_32FC1);
        
        try {
            // Perform template matching
            // Using TM_CCOEFF_NORMED for similarity scores between 0 and 1
            opencv_imgproc.matchTemplate(screen, pattern, result, TM_CCOEFF_NORMED);
            
            // Find matches above threshold
            if (options.isFindAll()) {
                return findAllMatches(result, pattern, options);
            } else {
                return findBestMatch(result, pattern, options);
            }
            
        } finally {
            result.release();
        }
    }
    
    /**
     * Finds all matches above the similarity threshold.
     */
    private List<PatternMatcher.MatchResult> findAllMatches(Mat result, Mat pattern, PatternMatcher.MatchOptions options) {
        List<PatternMatcher.MatchResult> matches = new ArrayList<>();
        double threshold = options.getSimilarity();
        int maxMatches = options.getMaxMatches();
        
        // Get result as Java array for easier processing
        float[] resultArray = new float[result.rows() * result.cols()];
        // Use FloatPointer to access the data
        FloatPointer dataPtr = new FloatPointer(result.data());
        dataPtr.get(resultArray);
        
        // Find all locations above threshold
        for (int y = 0; y < result.rows(); y++) {
            for (int x = 0; x < result.cols(); x++) {
                int idx = y * result.cols() + x;
                float score = resultArray[idx];
                
                if (score >= threshold) {
                    // Check for non-maximum suppression (avoid duplicate detections)
                    if (!isSupressed(matches, x, y, pattern.cols(), pattern.rows())) {
                        matches.add(new PatternMatcher.MatchResult(
                            x, y, 
                            pattern.cols(), pattern.rows(),
                            score
                        ));
                        
                        if (matches.size() >= maxMatches) {
                            return matches;
                        }
                    }
                }
            }
        }
        
        return matches;
    }
    
    /**
     * Finds the single best match.
     */
    private List<PatternMatcher.MatchResult> findBestMatch(Mat result, Mat pattern, PatternMatcher.MatchOptions options) {
        List<PatternMatcher.MatchResult> matches = new ArrayList<>();
        
        // Find min and max values
        DoublePointer minVal = new DoublePointer(1);
        DoublePointer maxVal = new DoublePointer(1);
        Point minLoc = new Point();
        Point maxLoc = new Point();
        
        opencv_core.minMaxLoc(result, minVal, maxVal, minLoc, maxLoc, null);
        
        // For TM_CCOEFF_NORMED, higher values are better matches
        double score = maxVal.get();
        
        if (score >= options.getSimilarity()) {
            matches.add(new PatternMatcher.MatchResult(
                maxLoc.x(), maxLoc.y(),
                pattern.cols(), pattern.rows(),
                score
            ));
        }
        
        // Clean up native pointers
        minVal.deallocate();
        maxVal.deallocate();
        
        return matches;
    }
    
    /**
     * Non-maximum suppression to avoid duplicate detections.
     */
    private boolean isSupressed(List<PatternMatcher.MatchResult> existing, int x, int y, int width, int height) {
        // Check if this match overlaps significantly with existing matches
        for (PatternMatcher.MatchResult match : existing) {
            int overlapX = Math.max(0, Math.min(x + width, match.getX() + match.getWidth()) - Math.max(x, match.getX()));
            int overlapY = Math.max(0, Math.min(y + height, match.getY() + match.getHeight()) - Math.max(y, match.getY()));
            int overlapArea = overlapX * overlapY;
            int matchArea = width * height;
            
            // If overlap is more than 50% of the area, suppress this detection
            if (overlapArea > matchArea * 0.5) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Converts BufferedImage to OpenCV Mat.
     */
    private Mat bufferedImageToMat(BufferedImage image) {
        // Convert BufferedImage to Mat using JavaCV utilities
        return Java2DFrameUtils.toMat(image);
    }
    
    @Override
    public boolean supportsPattern(io.github.jspinak.brobot.model.element.Pattern pattern) {
        // This implementation supports all standard patterns with BufferedImages
        return pattern != null && pattern.getBImage() != null;
    }
    
    @Override
    public String getImplementationName() {
        return "JavaCV/OpenCV";
    }
}