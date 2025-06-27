package io.github.jspinak.brobot.analysis.motion;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import io.github.jspinak.brobot.util.image.recognition.ImageLoader;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_core.CMP_NE;
import static org.bytedeco.opencv.global.opencv_core.bitwise_not;

/**
 * Finds dynamic and fixed pixels by comparing pixel values across multiple images.
 * This implementation of {@link FindDynamicPixels} uses direct pixel comparison
 * to identify changes, making it suitable for detecting any pixel-level differences.
 * 
 * <p>Key features:
 * <ul>
 * <li>Compares pixel values directly using OpenCV's compare function</li>
 * <li>Combines multiple comparisons with bitwise OR for comprehensive detection</li>
 * <li>Supports time-based capture for real-time motion analysis</li>
 * <li>Can capture screenshots at specified intervals for temporal analysis</li>
 * </ul></p>
 * 
 * <p>This approach is particularly effective for:
 * <ul>
 * <li>Detecting UI element changes</li>
 * <li>Monitoring screen regions for activity</li>
 * <li>Identifying animated or dynamic content</li>
 * </ul></p>
 * 
 * @see FindDynamicPixels
 * @see ColorMatrixUtilities
 * @see ImageLoader
 */
@Component
public class DynamicPixelFinder implements FindDynamicPixels {

    private final ColorMatrixUtilities matOps3d;
    private final ImageLoader getImage;

    /**
     * Constructs a DynamicPixelFinder with the specified utilities.
     * 
     * @param matOps3d utility for 3D matrix operations
     * @param getImage utility for capturing screen images
     */
    public DynamicPixelFinder(ColorMatrixUtilities matOps3d, ImageLoader getImage) {
        this.matOps3d = matOps3d;
        this.getImage = getImage;
    }

    /**
     * Creates a mask identifying pixels that change across a collection of images.
     * Compares each image to the first image using pixel-wise inequality comparison
     * and combines all differences with bitwise OR.
     * 
     * @param matVector collection of Mat objects to analyze (requires at least 2)
     * @return binary mask where dynamic pixels are marked (non-zero values)
     */
    public Mat getDynamicPixelMask(MatVector matVector) {
        int size = (int) matVector.size();
        if (size == 1) return new Mat(); // nothing to compare
        MatVector masks = new MatVector();
        Mat firstMat = matVector.get(0);
        for (int i=1; i<size; i++) {
            Mat dynamicPixels = matOps3d.cOmpare(firstMat, matVector.get(i), CMP_NE); // find pixels with different values
            masks.push_back(dynamicPixels);
        }
        Mat combinedMask = new Mat();
        for (Mat mask : masks.get()) {
            combinedMask = matOps3d.bItwise_or(mask, combinedMask);
        }
        return combinedMask;
    }

    /**
     * Creates a mask identifying pixels that remain unchanged across all images.
     * This is the inverse of the dynamic pixel mask.
     * 
     * @param matVector collection of Mat objects to analyze
     * @return binary mask where fixed pixels are marked (non-zero values)
     */
    public Mat getFixedPixelMask(MatVector matVector) {
        return matOps3d.bItwise_not(getDynamicPixelMask(matVector));
    }

    /**
     * Captures screenshots of a region over time and identifies changing pixels.
     * This method automates the process of collecting temporal data by taking
     * periodic screenshots and analyzing them for motion.
     * 
     * @param region the screen {@link Region} to monitor
     * @param intervalSeconds time between consecutive screenshots
     * @param totalSecondsToRun total duration to monitor the region
     * @return binary mask of pixels that changed during the observation period
     */
    public Mat getDynamicPixelMask(Region region, double intervalSeconds, double totalSecondsToRun) {
        MatVector matVector = getImage.getMatsFromScreen(region, intervalSeconds, totalSecondsToRun);
        return getDynamicPixelMask(matVector);
    }

    /**
     * Captures screenshots of a region over time and identifies unchanging pixels.
     * This method is useful for finding static UI elements or background areas
     * that remain constant while other parts of the screen change.
     * 
     * @param region the screen {@link Region} to monitor
     * @param intervalSeconds time between consecutive screenshots
     * @param totalSecondsToRun total duration to monitor the region
     * @return binary mask of pixels that remained unchanged during observation
     */
    public Mat getFixedPixelMask(Region region, double intervalSeconds, double totalSecondsToRun) {
        MatVector matVector = getImage.getMatsFromScreen(region, intervalSeconds, totalSecondsToRun);
        Mat dynamic = getDynamicPixelMask(matVector);
        Mat fixed = new Mat();
        bitwise_not(dynamic, fixed);
        return fixed;
    }

}
