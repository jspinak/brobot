package io.github.jspinak.brobot.analysis.motion;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Size;

import io.github.jspinak.brobot.util.image.core.MatrixUtilities;

import lombok.Getter;

/**
 * Detects pixel changes between multiple images using configurable image processing techniques.
 * This class provides a flexible pipeline for motion detection that can be customized based on
 * specific requirements.
 *
 * <p>The detection pipeline supports the following optional steps:
 *
 * <ul>
 *   <li>Grayscale conversion - Reduces computational complexity
 *   <li>Gaussian blur - Reduces noise and smooths images
 *   <li>Absolute difference - Finds pixel-level changes
 *   <li>Dilation - Expands and connects nearby changes
 *   <li>Thresholding - Creates binary mask of significant changes
 * </ul>
 *
 * <p>For multiple images (>2), the detector finds the maximum difference across all images,
 * ensuring no change is missed. Multi-channel images are handled by taking the maximum difference
 * across all channels.
 *
 * <p>Use the Builder pattern to configure the detection pipeline:
 *
 * <pre>{@code
 * PixelChangeDetector detector = new PixelChangeDetector.Builder()
 *     .setMats(images)
 *     .useGrayscale()
 *     .useThreshold(50, 255)
 *     .build();
 * Mat changeMask = detector.getChangeMask();
 * }</pre>
 *
 * @see MatrixUtilities
 * @see ChangedPixels
 */
@Getter
public class PixelChangeDetector {

    private List<Mat> originals = new ArrayList<>();

    /**
     * Flag to enable grayscale conversion. Converting to grayscale reduces computational complexity
     * by working with single-channel images instead of multi-channel (BGR/RGB).
     */
    private boolean useGrayscale;

    /** Grayscale versions of the original images. Only populated if useGrayscale is true. */
    private List<Mat> grays = new ArrayList<>();

    /**
     * Flag to enable Gaussian blur preprocessing. Gaussian blur reduces noise and smooths images,
     * which can help eliminate false positives from minor pixel variations.
     */
    private boolean useGaussianBlur;

    /** Width of the Gaussian kernel (default: 5) */
    private int gaussianWidth = 5;

    /** Height of the Gaussian kernel (default: 5) */
    private int gaussianHeight = 5;

    /** Standard deviation in X direction (0 = auto-calculate) */
    private int gaussianSigmaX = 0;

    /** Images after Gaussian blur processing */
    private List<Mat> gaussians = new ArrayList<>();

    /**
     * The absolute difference matrix showing pixel changes. Calculated after grayscale/Gaussian
     * processing but before dilation/threshold operations. For multiple images, contains the
     * maximum difference across all image pairs. This is a single-channel Mat.
     */
    private Mat absDiff;

    /**
     * Flag to enable morphological dilation. Dilation expands bright regions in the image, which
     * helps:
     *
     * <ul>
     *   <li>Connect nearby changed pixels into continuous regions
     *   <li>Fill small gaps in motion areas
     *   <li>Make motion regions more prominent
     * </ul>
     */
    private boolean useDilation;

    /** Number of rows in the dilation kernel (default: 5) */
    private int dilationRows = 5;

    /** Number of columns in the dilation kernel (default: 5) */
    private int dilationCols = 5;

    /** Type of the dilation kernel elements (default: 1) */
    private int dilationType = 1;

    /** Result of the dilation operation */
    private Mat dilation;

    /**
     * Flag to enable binary thresholding. Thresholding converts the grayscale difference image into
     * a binary mask where pixels above the threshold are white (255) and others are black (0). This
     * separates significant changes from minor variations.
     */
    private boolean useThreshold;

    /** Minimum threshold value - changes below this are ignored (default: 50) */
    private int threshMin = 50;

    /** Maximum value assigned to pixels above threshold (default: 255) */
    private int threshMax = 255;

    /** Binary mask after thresholding */
    private Mat threshold;

    /**
     * The final change detection mask. This single-channel Mat contains the result of the entire
     * processing pipeline, ready for use in motion detection.
     */
    private Mat changeMask;

    /**
     * Prints debug information for each stage of the processing pipeline. Useful for
     * troubleshooting and understanding how the detector processes images at each step.
     *
     * @param rows number of rows to print from each Mat
     * @param cols number of columns to print from each Mat
     * @param channels number of channels to print
     */
    public void print(int rows, int cols, int channels) {
        for (int i = 0; i < originals.size(); i++)
            MatrixUtilities.printPartOfMat(
                    originals.get(i), rows, cols, channels, "original #" + i);
        for (int i = 0; i < grays.size(); i++)
            MatrixUtilities.printPartOfMat(grays.get(i), rows, cols, channels, "gray #" + i);
        for (int i = 0; i < gaussians.size(); i++)
            MatrixUtilities.printPartOfMat(
                    gaussians.get(i), rows, cols, channels, "gaussian #" + i);
        MatrixUtilities.printPartOfMat(absDiff, rows, cols, channels, "absDiff");
        MatrixUtilities.printPartOfMat(dilation, rows, cols, channels, "dilation");
        MatrixUtilities.printPartOfMat(threshold, rows, cols, channels, "threshold");
        MatrixUtilities.printPartOfMat(changeMask, rows, cols, channels, "finalMat");
    }

    /**
     * Builder for creating customized PixelChangeDetector instances. Allows flexible configuration
     * of the image processing pipeline by enabling/disabling specific operations and setting their
     * parameters.
     *
     * <p>Example usage:
     *
     * <pre>{@code
     * PixelChangeDetector detector = new PixelChangeDetector.Builder()
     *     .setMats(imageList)
     *     .useGrayscale()
     *     .useGaussianBlur(5, 5, 0)
     *     .useThreshold(50, 255)
     *     .build();
     * }</pre>
     */
    public static class Builder {
        private List<Mat> originals = new ArrayList<>();
        private boolean useGrayscale;
        private List<Mat> grays = new ArrayList<>();
        private boolean useGaussianBlur;
        private int gaussianWidth = 5;
        private int gaussianHeight = 5;
        private int gaussianSigmaX = 0;
        private List<Mat> gaussians = new ArrayList<>();
        private Mat absDiff = new Mat();
        private boolean useDilation;
        private int dilationRows = 5;
        private int dilationCols = 5;
        private int dilationType = 1;
        private Mat dilation = new Mat();
        private boolean useThreshold;
        private int threshMin = 50;
        private int threshMax = 255;
        private Mat threshold = new Mat();
        private Mat finalMat = new Mat();

        /**
         * Enables grayscale conversion in the processing pipeline. This reduces computational
         * complexity and is recommended when color information is not critical for motion
         * detection.
         *
         * @return this Builder instance for method chaining
         */
        public Builder useGrayscale() {
            this.useGrayscale = true;
            return this;
        }

        /**
         * Enables Gaussian blur with specified kernel parameters. Gaussian blur reduces noise and
         * can help eliminate false positives from minor pixel variations.
         *
         * @param width kernel width (must be positive and odd)
         * @param height kernel height (must be positive and odd)
         * @param sigmaX standard deviation in X direction (0 for auto-calculation)
         * @return this Builder instance for method chaining
         */
        public Builder useGaussianBlur(int width, int height, int sigmaX) {
            this.useGaussianBlur = true;
            this.gaussianWidth = width;
            this.gaussianHeight = height;
            this.gaussianSigmaX = sigmaX;
            return this;
        }

        /**
         * Enables morphological dilation with specified kernel parameters. Dilation expands changed
         * regions, helping to connect nearby changes and create more continuous motion areas.
         *
         * @param rows number of rows in the dilation kernel
         * @param cols number of columns in the dilation kernel
         * @param type data type of kernel elements
         * @return this Builder instance for method chaining
         */
        public Builder useDilation(int rows, int cols, int type) {
            this.useDilation = true;
            this.dilationRows = rows;
            this.dilationCols = cols;
            this.dilationType = type;
            return this;
        }

        /**
         * Enables binary thresholding with specified parameters. Thresholding creates a binary mask
         * where significant changes are white and minor variations are black.
         *
         * @param min minimum threshold value (typically 20-100)
         * @param max value assigned to pixels above threshold (typically 255)
         * @return this Builder instance for method chaining
         */
        public Builder useThreshold(int min, int max) {
            this.useThreshold = true;
            this.threshMin = min;
            this.threshMax = max;
            return this;
        }

        private void doGrayscale() {
            if (!useGrayscale) return;
            this.originals.forEach(og -> grays.add(MatrixUtilities.getGrayscale(og)));
        }

        private void doGaussians(List<Mat> mats) {
            mats.forEach(
                    mat -> {
                        Mat gaussian = new Mat();
                        GaussianBlur(
                                mat,
                                gaussian,
                                new Size(this.gaussianWidth, this.gaussianHeight),
                                this.gaussianSigmaX);
                        gaussians.add(gaussian);
                    });
        }

        private void doGaussians() {
            if (!useGaussianBlur) return;
            if (!grays.isEmpty()) doGaussians(grays);
            else doGaussians(originals);
        }

        /**
         * Calculates the absolute difference across multiple images. For more than two images,
         * finds the maximum difference by comparing the minimum and maximum pixel values at each
         * location. For multi-channel images, takes the maximum difference across all channels.
         *
         * @param mats list of images to compare
         */
        private void doAbsoluteDifference(List<Mat> mats) {
            if (mats == null || mats.isEmpty()) {
                System.err.println("Warning: No mats provided for absolute difference");
                absDiff = new Mat(); // Create empty Mat to prevent NPE later
                return;
            }

            // Validate input mats
            for (Mat mat : mats) {
                if (mat == null || mat.empty()) {
                    System.err.println(
                            "Warning: Null or empty Mat in list, skipping absolute difference");
                    absDiff = new Mat();
                    return;
                }
            }

            try {
                Mat min = MatrixUtilities.getNewMatWithPerCellMinsOrMaxes(mats, REDUCE_MIN);
                Mat max = MatrixUtilities.getNewMatWithPerCellMinsOrMaxes(mats, REDUCE_MAX);

                if (min == null || min.empty() || max == null || max.empty()) {
                    System.err.println("Warning: Failed to compute min/max mats");
                    absDiff = new Mat();
                    return;
                }

                // Initialize absDiff if needed
                if (absDiff == null) {
                    absDiff = new Mat();
                }

                absdiff(min, max, absDiff);
                absDiff = MatrixUtilities.getMinOrMaxPerCellAcrossChannels(absDiff, REDUCE_MAX);
            } catch (Exception e) {
                System.err.println("Error computing absolute difference: " + e.getMessage());
                e.printStackTrace();
                absDiff = new Mat(); // Ensure absDiff is not null
            }
        }

        private void doAbsoluteDifference() {
            if (!gaussians.isEmpty()) doAbsoluteDifference(gaussians);
            else if (!grays.isEmpty()) doAbsoluteDifference(grays);
            else doAbsoluteDifference(originals);
        }

        private void doDilation() {
            if (useDilation) {
                // Add defensive checks to prevent crash
                if (absDiff == null || absDiff.empty()) {
                    System.err.println("Warning: absDiff is null or empty, skipping dilation");
                    return;
                }

                // Initialize dilation Mat if needed
                if (dilation == null || dilation.empty()) {
                    dilation = new Mat();
                }

                // Create kernel safely
                Mat kernel = Mat.ones(dilationRows, dilationCols, dilationType).asMat();
                if (kernel == null || kernel.empty()) {
                    System.err.println("Warning: Failed to create dilation kernel");
                    return;
                }

                try {
                    dilate(absDiff, dilation, kernel);
                } catch (Exception e) {
                    System.err.println("Error during dilation: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // Always release the kernel
                    if (kernel != null && !kernel.isNull()) {
                        kernel.release();
                    }
                }
            }
        }

        private void doThreshold() {
            if (useThreshold) {
                if (useDilation)
                    threshold(dilation, threshold, this.threshMin, this.threshMax, THRESH_BINARY);
                else threshold(absDiff, threshold, this.threshMin, this.threshMax, THRESH_BINARY);
            }
        }

        private void doFinal() {
            if (useThreshold) finalMat = threshold.clone();
            else if (useDilation) finalMat = dilation.clone();
            else finalMat = absDiff.clone();
        }

        /**
         * Adds images to the existing collection for change detection.
         *
         * @param mats variable number of Mat objects to add
         * @return this Builder instance for method chaining
         */
        public Builder addMats(Mat... mats) {
            this.originals.addAll(Arrays.asList(mats));
            return this;
        }

        /**
         * Adds a list of images to the existing collection.
         *
         * @param mats list of Mat objects to add
         * @return this Builder instance for method chaining
         */
        public Builder addMats(List<Mat> mats) {
            this.originals.addAll(mats);
            return this;
        }

        /**
         * Sets the images for change detection, replacing any existing images.
         *
         * @param mats list of Mat objects to analyze
         * @return this Builder instance for method chaining
         */
        public Builder setMats(List<Mat> mats) {
            this.originals = mats;
            return this;
        }

        /**
         * Sets the images from a MatVector, replacing any existing images.
         *
         * @param mats MatVector containing images to analyze
         * @return this Builder instance for method chaining
         */
        public Builder setMats(MatVector mats) {
            this.originals.addAll(Arrays.asList(mats.get()));
            return this;
        }

        private void calculateAllMats() {
            doGrayscale();
            doGaussians();
            doAbsoluteDifference();
            doDilation();
            doThreshold();
            doFinal();
        }

        /**
         * Builds and returns a configured PixelChangeDetector instance. Executes the entire image
         * processing pipeline based on the configured options and produces the final change mask.
         *
         * @return a new PixelChangeDetector with processed results
         */
        public PixelChangeDetector build() {
            PixelChangeDetector pixelChangeDetector = new PixelChangeDetector();
            pixelChangeDetector.originals = this.originals;
            pixelChangeDetector.useGrayscale = this.useGrayscale;
            pixelChangeDetector.useGaussianBlur = this.useGaussianBlur;
            pixelChangeDetector.gaussianWidth = this.gaussianWidth;
            pixelChangeDetector.gaussianHeight = this.gaussianHeight;
            pixelChangeDetector.gaussianSigmaX = this.gaussianSigmaX;
            pixelChangeDetector.useDilation = this.useDilation;
            pixelChangeDetector.dilationRows = this.dilationRows;
            pixelChangeDetector.dilationCols = this.dilationCols;
            pixelChangeDetector.dilationType = this.dilationType;
            pixelChangeDetector.useThreshold = this.useThreshold;
            pixelChangeDetector.threshMin = this.threshMin;
            pixelChangeDetector.threshMax = this.threshMax;
            calculateAllMats();
            pixelChangeDetector.grays = this.grays;
            pixelChangeDetector.gaussians = this.gaussians;
            pixelChangeDetector.absDiff = this.absDiff;
            pixelChangeDetector.dilation = this.dilation;
            pixelChangeDetector.threshold = this.threshold;
            pixelChangeDetector.changeMask = this.finalMat;
            return pixelChangeDetector;
        }
    }
}
