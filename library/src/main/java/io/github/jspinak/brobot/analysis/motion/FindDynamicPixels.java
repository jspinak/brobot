package io.github.jspinak.brobot.analysis.motion;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;

/**
 * Interface for identifying dynamic (changing) and fixed (unchanging) pixels
 * across a sequence of images. Implementations analyze temporal changes to
 * create binary masks distinguishing moving from stationary content.
 * 
 * <p>This interface is fundamental to motion detection in Brobot, enabling:
 * <ul>
 * <li>Motion-based object tracking</li>
 * <li>Activity detection in screen regions</li>
 * <li>Separation of foreground from background elements</li>
 * <li>Identification of animated UI components</li>
 * </ul></p>
 * 
 * <p>Implementations may use various techniques such as:
 * <ul>
 * <li>Frame differencing</li>
 * <li>Pixel-wise comparison</li>
 * <li>Statistical analysis of pixel values</li>
 * <li>Threshold-based change detection</li>
 * </ul></p>
 * 
 * @see ChangedPixels
 * @see MotionDetector
 * @see DynamicPixelFinder
 */
public interface FindDynamicPixels {

    /**
     * Creates a binary mask identifying pixels that change across the image sequence.
     * Dynamic pixels typically represent moving objects, animations, or changing content.
     * 
     * @param matVector collection of images to analyze for temporal changes
     * @return binary Mat where dynamic pixels are marked (typically as 255/white)
     */
    Mat getDynamicPixelMask(MatVector matVector);
    
    /**
     * Creates a binary mask identifying pixels that remain constant across the image sequence.
     * Fixed pixels typically represent static backgrounds or unchanging UI elements.
     * 
     * @param matVector collection of images to analyze for temporal changes
     * @return binary Mat where fixed pixels are marked (typically as 255/white)
     */
    Mat getFixedPixelMask(MatVector matVector);
}
