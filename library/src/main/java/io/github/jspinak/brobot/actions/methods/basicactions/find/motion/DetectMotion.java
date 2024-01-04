package io.github.jspinak.brobot.actions.methods.basicactions.find.motion;

import io.github.jspinak.brobot.imageUtils.MatOps;
import io.github.jspinak.brobot.imageUtils.MatOps3d;
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

@Component
public class DetectMotion implements FindDynamicPixels {

    private final MatOps3d matOps3d;

    public DetectMotion(MatOps3d matOps3d) {
        this.matOps3d = matOps3d;
    }

    /**
     * Detect movement between image1 and image2
     * @param image1 the first image
     * @param image2 the second image
     * @return a Mat of the same size and channels as the input Mat(s)
     */
    public Mat getDynamicPixelMask(Mat image1, Mat image2) {
        Mat gray1 = MatOps.getGrayscale(image1);
        Mat gray2 = MatOps.getGrayscale(image2);
        Mat gauss1 = new Mat();
        Mat gauss2 = new Mat();
        /*
        Apply Gaussian blur to the grayscale images (gray1 and gray2) using a kernel size of 5x5 and standard deviation
        of 0. This helps smooth the images and reduce noise.
         */
        GaussianBlur(gray1, gauss1, new Size(5,5), 0);
        GaussianBlur(gray2, gauss2, new Size(5,5), 0);
        /*
        Compute the absolute difference between the two blurred images (gauss1 and gauss2) using the absdiff function.
        This results in a grayscale image (absDiff) highlighting the pixel-wise differences between the two input images.
         */
        Mat absDiff = new Mat();
        absdiff(gauss1, gauss2, absDiff);
        /*
        Dilation Operation: For each pixel in the binary image (absDiff), the maximum pixel value in the neighborhood
        defined by the kernel is computed. If any pixel in the neighborhood has a value of 1 (white), the result at
        the corresponding position in the output image (dilated) will be set to 1.
        Effect: Dilation tends to increase the size of bright regions, making them more prominent and connected.
        It can be useful for filling small gaps, joining nearby contours, or accentuating features in an image.
         */
        //Mat kernel = new Mat(Mat.ones(5, 5, 1));
        //Mat dilated = new Mat();
        //dilate(absDiff, dilated, kernel); // dilating the Mat makes differences more noticeable and contour detection easier
        Mat binary = new Mat();
        /*
        Create a binary mask (binary) by thresholding the absolute difference image (absDiff). Pixels with a difference
        value greater than or equal to thresh=50 are set to 255 (white), while others are set to 0 (black). This step
        effectively separates regions with significant differences from those with minor differences.
         */
        threshold(absDiff, binary, 50, 255, THRESH_BINARY);
        return binary;
    }

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

    public Mat getFixedPixelMask(MatVector matVector) {
        return matOps3d.bItwise_not(getDynamicPixelMask(matVector));
    }

}
