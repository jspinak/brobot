package io.github.jspinak.brobot.analysis.motion;

import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.absdiff;
import static org.bytedeco.opencv.global.opencv_core.bitwise_or;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * Detects motion between images using frame differencing techniques.
 * This implementation of {@link FindDynamicPixels} uses Gaussian blur
 * and absolute difference calculations to identify moving pixels.
 * 
 * <p>The motion detection process involves:
 * <ul>
 * <li>Converting images to grayscale for efficient processing</li>
 * <li>Applying Gaussian blur to reduce noise</li>
 * <li>Computing absolute differences between frames</li>
 * <li>Thresholding to create binary motion masks</li>
 * <li>Combining multiple frame differences with bitwise OR</li>
 * </ul></p>
 * 
 * <p>This approach is effective for detecting movement in video sequences
 * or comparing images taken at different times from the same viewpoint.</p>
 * 
 * @see FindDynamicPixels
 * @see MatrixUtilities
 * @see ColorMatrixUtilities
 */
@Component
public class MotionDetector implements FindDynamicPixels {

    private final ColorMatrixUtilities matOps3d;

    /**
     * Constructs a MotionDetector instance with the specified matrix operations utility.
     * 
     * @param matOps3d utility for 3D matrix operations including bitwise operations
     */
    public MotionDetector(ColorMatrixUtilities matOps3d) {
        this.matOps3d = matOps3d;
    }

    /**
     * Detects pixels that have changed between two images.
     * This method applies Gaussian blur to reduce noise, computes the absolute
     * difference between the images, and thresholds the result to create a
     * binary mask of changed pixels.
     * 
     * @param image1 the first image to compare
     * @param image2 the second image to compare
     * @return binary Mat where moving pixels are white (255) and static pixels are black (0)
     */
    public Mat getDynamicPixelMask(Mat image1, Mat image2) {
        Mat gray1 = MatrixUtilities.getGrayscale(image1);
        Mat gray2 = MatrixUtilities.getGrayscale(image2);
        Mat gauss1 = new Mat();
        Mat gauss2 = new Mat();
        // Apply Gaussian blur to reduce noise (5x5 kernel, standard deviation of 0)
        GaussianBlur(gray1, gauss1, new Size(5,5), 0);
        GaussianBlur(gray2, gauss2, new Size(5,5), 0);
        
        // Compute absolute difference between blurred images
        Mat absDiff = new Mat();
        absdiff(gauss1, gauss2, absDiff);
        
        // Optional dilation to make differences more prominent (currently commented out)
        //Mat kernel = new Mat(Mat.ones(5, 5, 1));
        //Mat dilated = new Mat();
        //dilate(absDiff, dilated, kernel);
        
        // Create binary mask by thresholding (threshold=50)
        Mat binary = new Mat();
        threshold(absDiff, binary, 50, 255, THRESH_BINARY);
        return binary;
    }

    /**
     * Detects pixels that change across a sequence of images.
     * Compares each image to the first image and combines all detected
     * changes using bitwise OR to create a comprehensive motion mask.
     * 
     * @param matVector collection of images to analyze (requires at least 2 images)
     * @return binary Mat where any pixel that moved in any frame is white (255)
     */
    public Mat getDynamicPixelMask(MatVector matVector) {
        if (matVector == null || matVector.size() < 2) return new Mat();
        List<Mat> absdiffs = new ArrayList<>();
        for (int i=1; i<matVector.size(); i++) {
            absdiffs.add(getDynamicPixelMask(matVector.get(0), matVector.get(i)));
        }
        // absdiffs are now 1D Mat(s)
        Mat combinedMask = absdiffs.get(0);
        for (Mat mask : absdiffs) {
            bitwise_or(combinedMask, mask, combinedMask); //matOps3d.bItwise_or(mask, combinedMask);
        }
        // make all positive values 255
        Mat dynamicMask = new Mat();
        opencv_imgproc.threshold(combinedMask, dynamicMask, 0, 255, opencv_imgproc.THRESH_BINARY);
        return dynamicMask;
    }

    /**
     * Creates a mask identifying pixels that remain stationary across the image sequence.
     * This is the inverse of the dynamic pixel mask.
     * 
     * @param matVector collection of images to analyze
     * @return binary Mat where stationary pixels are white (255) and moving pixels are black (0)
     */
    public Mat getFixedPixelMask(MatVector matVector) {
        return matOps3d.bItwise_not(getDynamicPixelMask(matVector));
    }

}
