package io.github.jspinak.brobot.util.image.core;

import static org.bytedeco.opencv.global.opencv_core.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Pattern;
// Removed old logging import: 
import lombok.extern.slf4j.Slf4j;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;


/**
 * Extensions of OpenCV operations to handle 3-channel (color) images.
 *
 * <p>This component provides 3-channel versions of OpenCV operations that normally only work with
 * single-channel images. It enables channel-wise processing while maintaining the multi-channel
 * structure, essential for color image analysis.
 *
 * <p>Key operation categories:
 *
 * <ul>
 *   <li><b>Statistical operations</b>: mean, standard deviation, min/max per channel
 *   <li><b>Comparison operations</b>: channel-wise comparisons with masks
 *   <li><b>Bitwise operations</b>: AND, OR, NOT operations on 3-channel images
 *   <li><b>Clustering</b>: K-means clustering with per-channel processing
 *   <li><b>Index tracking</b>: Finding min/max indices across channels
 * </ul>
 *
 * <p>Design philosophy:
 *
 * <ul>
 *   <li>Split multi-channel images into separate channels
 *   <li>Apply single-channel operations to each channel
 *   <li>Merge results back into multi-channel format
 *   <li>Preserve channel correspondence throughout processing
 * </ul>
 *
 * <p>Method naming convention:
 *
 * <ul>
 *   <li>Lowercase letter variations (e.g., sPlit, mErge) differentiate from OpenCV functions
 *   <li>This avoids confusion with static OpenCV methods
 * </ul>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Color-based object detection and segmentation
 *   <li>Multi-channel statistical analysis
 *   <li>Color clustering and quantization
 *   <li>Channel-wise image comparisons
 * </ul>
 *
 * <p>Thread safety: Methods are stateless and thread-safe.
 *
 * @see MatrixUtilities
 * @see BufferedImageUtilities
 */
@Component
@Slf4j
public class ColorMatrixUtilities {

    private final BufferedImageUtilities bufferedImageOps;

    public ColorMatrixUtilities(BufferedImageUtilities bufferedImageOps) {
        this.bufferedImageOps = bufferedImageOps;
    }

    /**
     * Concatenates multiple 3-channel images into a single Mat with one column per channel.
     *
     * <p>This method is designed for aggregating pixels from multiple images of potentially
     * different sizes into a unified format for analysis. Each channel is reshaped into a single
     * column and vertically concatenated.
     *
     * <p>Processing steps:
     *
     * <ol>
     *   <li>Reshape each Mat into a single column (total pixels × 1)
     *   <li>Split into individual channels
     *   <li>Vertically concatenate corresponding channels
     *   <li>Merge back into 3-channel format
     * </ol>
     *
     * <p>Output format:
     *
     * <ul>
     *   <li>Width: 3 (one column per channel)
     *   <li>Height: Sum of all pixel counts from input images
     *   <li>Channels: 3 (BGR or HSV maintained)
     * </ul>
     *
     * <p>Use case: Statistical analysis across multiple images where spatial information can be
     * discarded but channel values must be preserved.
     *
     * @param mats list of 3-channel Mats to concatenate
     * @return concatenated Mat with 3 columns (one per channel), empty Mat if input is empty
     */
    public Mat vConcatToSingleColumnPerChannel(List<Mat> mats) {
        if (mats.isEmpty()) return new Mat();

        // Validate input mats
        boolean hasValidMat = false;
        for (Mat mat : mats) {
            if (mat != null && !mat.empty() && mat.channels() > 0) {
                hasValidMat = true;
                break;
            }
        }
        if (!hasValidMat) {
            log.warn("No valid Mats found in vConcatToSingleColumnPerChannel");
            return new Mat();
        }

        MatVector columnMats = new MatVector(3);
        // Initialize empty Mats for each channel
        for (int i = 0; i < 3; i++) {
            columnMats.put(i, new Mat());
        }

        boolean firstValidMat = true;
        for (int m = 0; m < mats.size(); m++) {
            Mat currentMat = mats.get(m);

            // Skip invalid mats
            if (currentMat == null || currentMat.empty() || currentMat.channels() == 0) {
                log.debug("Skipping invalid Mat at index {}", m);
                continue;
            }

            Mat colMat = currentMat.reshape(0, (int) currentMat.total());

            // Check if the reshaped mat has the expected channels
            if (colMat.channels() < 3) {
                log.warn(
                        "Mat at index {} has only {} channels after reshape, expected 3. Skipping.",
                        m,
                        colMat.channels());
                continue;
            }

            MatVector colMatVec = sPlit(colMat);

            // Verify split was successful
            if (colMatVec.size() < 3) {
                log.warn(
                        "Split produced only {} channels at index {}, expected 3. Skipping.",
                        colMatVec.size(),
                        m);
                continue;
            }

            for (int i = 0; i < 3; i++) {
                if (firstValidMat) {
                    columnMats.put(i, colMatVec.get(i));
                } else {
                    vconcat(columnMats.get(i), colMatVec.get(i), columnMats.get(i));
                }
            }
            firstValidMat = false;
        }

        // Check if we processed any valid mats
        if (firstValidMat) {
            log.warn("No valid 3-channel Mats were processed in vConcatToSingleColumnPerChannel");
            return new Mat();
        }

        // Horizontally concatenate the 3 single-channel columns
        Mat result = new Mat();
        hconcat(columnMats, result);
        return result;
    }

    /**
     * Performs k-means clustering independently on each channel of a 3-channel image.
     *
     * <p>Unlike standard k-means which treats pixels as 3D points in color space, this method
     * clusters each channel separately, allowing for independent analysis of color components.
     *
     * <p>Processing approach:
     *
     * <ol>
     *   <li>Convert image to CV_32F format (required by k-means)
     *   <li>Split into individual channels
     *   <li>Run k-means on each channel independently
     *   <li>Merge channel results into 3-channel labels and centers
     * </ol>
     *
     * <p>Output interpretation:
     *
     * <ul>
     *   <li>Labels: 3-channel Mat where each channel contains cluster assignments
     *   <li>Centers: 3-channel Mat with cluster centers for each channel
     *   <li>Compactness: Array of 3 values indicating clustering quality per channel
     * </ul>
     *
     * <p>Use cases:
     *
     * <ul>
     *   <li>Analyzing color distribution per channel
     *   <li>Channel-specific quantization
     *   <li>Identifying dominant values in each color component
     * </ul>
     *
     * @param image the 3-channel image to cluster
     * @param numberOfCenters number of clusters (k value)
     * @param labels output 3-channel Mat with cluster assignments
     * @param termCriteria convergence criteria for k-means algorithm
     * @param attempts number of times to run k-means with different initializations
     * @param centers output 3-channel Mat with cluster centers
     * @return array of 3 compactness scores (sum of squared distances), one per channel
     */
    public double[] kMeans(
            Mat image,
            int numberOfCenters,
            Mat labels,
            TermCriteria termCriteria,
            int attempts,
            Mat centers) {
        Mat imageCV_32F = new Mat();
        image.convertTo(imageCV_32F, CV_32F);
        MatVector channels = new MatVector(3);
        split(imageCV_32F, channels);
        MatVector centersVector = new MatVector(3);
        MatVector labelsVector = new MatVector(3);
        double[] compactness = new double[3];
        for (int i = 0; i < 3; i++) {
            Mat channelLabels = new Mat();
            Mat channelCenters = new Mat();
            compactness[i] =
                    kmeans(
                            channels.get(i),
                            numberOfCenters,
                            channelLabels,
                            termCriteria,
                            attempts,
                            KMEANS_PP_CENTERS,
                            channelCenters);
            labelsVector.put(i, channelLabels);
            centersVector.put(i, channelCenters);
        }
        merge(labelsVector, labels);
        merge(centersVector, centers);
        return compactness;
    }

    /**
     * Compares each channel of an image against corresponding threshold values.
     *
     * <p>Performs element-wise comparison between each channel and its corresponding value in the
     * comparison array. Each channel is compared independently.
     *
     * <p>Comparison operators (from OpenCV):
     *
     * <ul>
     *   <li>CMP_EQ: Equal to
     *   <li>CMP_GT: Greater than
     *   <li>CMP_GE: Greater than or equal
     *   <li>CMP_LT: Less than
     *   <li>CMP_LE: Less than or equal
     *   <li>CMP_NE: Not equal
     * </ul>
     *
     * <p>Result format:
     *
     * <ul>
     *   <li>True pixels: 255
     *   <li>False pixels: 0
     *   <li>Each channel compared independently
     * </ul>
     *
     * @param src the 3-channel source image
     * @param cmpTo array of 3 comparison values (one per channel)
     * @param cmpop comparison operator constant from OpenCV
     * @return 3-channel binary mask with comparison results
     */
    public Mat cOmpare(Mat src, double[] cmpTo, int cmpop) {
        MatVector channels = new MatVector(3);
        split(src, channels);
        MatVector maskVector = new MatVector(3);
        for (int i = 0; i < 3; i++) {
            Mat channelMask = new Mat(src.size(), CV_8UC1);
            compare(channels.get(i), new Mat(new Scalar(cmpTo[i])), channelMask, cmpop);
            maskVector.put(i, channelMask);
        }
        Mat mask = new Mat();
        merge(maskVector, mask);
        return mask;
    }

    /**
     * Performs element-wise comparison between corresponding channels of two Mats.
     *
     * <p>Compares each pixel in each channel of the source against the corresponding pixel in the
     * comparison Mat. Handles cases where Mats have different channel counts by using the minimum.
     *
     * <p>Flexibility:
     *
     * <ul>
     *   <li>Supports different channel counts (uses minimum)
     *   <li>Each channel compared independently
     *   <li>Result written to provided destination Mat
     * </ul>
     *
     * <p>Common use cases:
     *
     * <ul>
     *   <li>Finding pixels brighter/darker than reference
     *   <li>Detecting changes between frames
     *   <li>Creating masks for conditional operations
     * </ul>
     *
     * @param src the source Mat (any number of channels)
     * @param cmpTo the comparison Mat (any number of channels)
     * @param dst destination Mat for the result mask (will be allocated if needed)
     * @param cmpop comparison operator (CMP_EQ, CMP_GT, etc.)
     * @return the dst Mat containing comparison results
     */
    public Mat cOmpare(Mat src, Mat cmpTo, Mat dst, int cmpop) {
        int numberOfChannels = Math.min(src.channels(), cmpTo.channels());
        MatVector channels = new MatVector(numberOfChannels);
        split(src, channels);
        MatVector cmpToChannels = new MatVector(numberOfChannels);
        split(cmpTo, cmpToChannels);
        MatVector maskVector = new MatVector(numberOfChannels);
        for (int i = 0; i < numberOfChannels; i++) {
            Mat channelMask = new Mat(src.size(), CV_8UC1);
            compare(channels.get(i), cmpToChannels.get(i), channelMask, cmpop);
            maskVector.put(i, channelMask);
        }
        merge(maskVector, dst);
        return dst;
    }

    /**
     * Convenience method for channel-wise comparison without pre-allocated destination.
     *
     * <p>Creates and returns a new Mat containing the comparison results. Equivalent to calling
     * cOmpare(src, cmpTo, dst, cmpop) with a new dst Mat.
     *
     * @param src first Mat to compare
     * @param cmpTo second Mat to compare
     * @param cmpop comparison operator (CMP_EQ, CMP_GT, etc.)
     * @return new 3-channel binary mask with comparison results
     */
    public Mat cOmpare(Mat src, Mat cmpTo, int cmpop) {
        Mat mat = new Mat();
        cOmpare(src, cmpTo, mat, cmpop);
        return mat;
    }

    /**
     * Splits a multi-channel Mat into separate single-channel Mats.
     *
     * <p>Wrapper around OpenCV's split function that returns the MatVector directly. The method
     * name uses mixed case to differentiate from the static OpenCV function.
     *
     * @param src multi-channel Mat to split
     * @return MatVector containing individual channels
     */
    public MatVector sPlit(Mat src) {
        MatVector channels = new MatVector(src.channels());
        split(src, channels);
        return channels;
    }

    /**
     * Extracts the first channel from a multi-channel Mat.
     *
     * <p>Convenience method for accessing just the first channel, commonly used when processing
     * grayscale conversions or when only one channel is needed.
     *
     * @param src multi-channel Mat
     * @return single-channel Mat containing the first channel
     */
    public Mat getFirstChannel(Mat src) {
        MatVector matVector = sPlit(src);
        Mat firstChannel = matVector.get(0);
        return firstChannel;
    }

    /**
     * Merges separate channels into a single multi-channel Mat.
     *
     * <p>Wrapper around OpenCV's merge function that creates and returns the result Mat. The method
     * name uses mixed case to differentiate from the static OpenCV function.
     *
     * @param matVector vector of single-channel Mats to merge
     * @return multi-channel Mat containing all input channels
     */
    public Mat mErge(MatVector matVector) {
        Mat merged = new Mat();
        merge(matVector, merged);
        return merged;
    }

    /**
     * Computes mean and standard deviation for each channel using a 3-channel mask.
     *
     * <p>OpenCV's meanStdDev only accepts single-channel masks, but this method enables using a
     * different mask for each channel, allowing for more precise statistical calculations.
     *
     * <p>Processing approach:
     *
     * <ol>
     *   <li>Split both source and mask into channels
     *   <li>Apply meanStdDev to each channel with its corresponding mask
     *   <li>Merge channel statistics back into 3-channel format
     * </ol>
     *
     * <p>Note: The implementation appears to have redundant calls to meanStdDev on lines 180-182
     * that may be legacy code.
     *
     * <p>Use case: Computing color statistics for specific regions where each channel may have
     * different areas of interest.
     *
     * @param src the 3-channel image to analyze
     * @param mask 3-channel mask where each channel masks its corresponding color channel
     * @return MatVector with mean at index 0 and stddev at index 1, both as 3-channel Mats
     */
    public MatVector mEanStdDev(Mat src, Mat mask) {
        MatVector meanStddev = new MatVector(2);
        if (src.empty()) {
            meanStddev.put(0, new Mat());
            meanStddev.put(1, new Mat());
            return meanStddev;
        }
        Mat mean = new Mat(src.size(), src.type());
        Mat stddev = new Mat(src.size(), src.type());
        MatVector maskVector = sPlit(mask);
        // Remove redundant meanStdDev calls that were overwriting results
        // meanStdDev(src, mean, stddev, maskVector.get(0));
        // meanStdDev(src, mean, stddev, maskVector.get(1));
        // meanStdDev(src, mean, stddev, maskVector.get(2));
        MatVector srcVector = sPlit(src);
        MatVector meanVector = new MatVector(3);
        MatVector stddevVector = new MatVector(3);
        for (int i = 0; i < 3; i++) {
            Mat channelMean = new Mat();
            Mat channelStddev = new Mat();
            meanStdDev(srcVector.get(i), channelMean, channelStddev, maskVector.get(i));
            meanVector.put(i, channelMean);
            stddevVector.put(i, channelStddev);
        }
        merge(meanVector, mean);
        merge(stddevVector, stddev);
        meanStddev.put(0, mean);
        meanStddev.put(1, stddev);
        return meanStddev;
    }

    /**
     * Finds minimum and maximum values for each channel using a 3-channel mask.
     *
     * <p>Extends OpenCV's minMaxLoc to work with multi-channel images and masks, computing min/max
     * values independently for each channel using its corresponding mask channel.
     *
     * <p>Output format:
     *
     * <ul>
     *   <li>min[0]: minimum value in channel 0 (B in BGR)
     *   <li>min[1]: minimum value in channel 1 (G in BGR)
     *   <li>min[2]: minimum value in channel 2 (R in BGR)
     *   <li>max array follows same pattern
     * </ul>
     *
     * <p>Note: Location information (minLoc, maxLoc) is computed but discarded. If locations are
     * needed, consider modifying this method.
     *
     * @param src 3-channel source image
     * @param min DoublePointer with space for 3 values to store minimums
     * @param max DoublePointer with space for 3 values to store maximums
     * @param mask 3-channel mask for selective min/max computation
     */
    public void minMax(Mat src, DoublePointer min, DoublePointer max, Mat mask) {
        MatVector channels = new MatVector(3);
        MatVector masks = new MatVector(3);
        split(src, channels);
        split(mask, masks);
        for (int i = 0; i < 3; i++) {
            DoublePointer channelMin = new DoublePointer(1);
            DoublePointer channelMax = new DoublePointer(1);
            Point minLoc = new Point();
            Point maxLoc = new Point();
            minMaxLoc(channels.get(i), channelMin, channelMax, minLoc, maxLoc, masks.get(i));
            min.put(i, channelMin.get(0));
            max.put(i, channelMax.get(0));
        }
    }

    /**
     * Updates minimum score tracking with new challenger values.
     *
     * <p>Maintains a running minimum across multiple images by comparing current best scores with a
     * challenger and updating both the scores and their source indices where the challenger wins.
     *
     * <p>Algorithm:
     *
     * <ol>
     *   <li>Compare challenger against current best scores
     *   <li>Create mask where challenger has lower values
     *   <li>Update indices to challengerIndex where mask is true
     *   <li>Update bestScores with challenger values where mask is true
     * </ol>
     *
     * <p>Use case: Finding the best matching template across multiple candidates where lower scores
     * indicate better matches.
     *
     * @param minIndices Mat storing the index of the image with minimum score at each pixel
     * @param bestScores Mat storing the current minimum scores
     * @param challenger Mat with new scores to compare
     * @param challengerIndex index identifier for the challenger image
     */
    public void minIndex(Mat minIndices, Mat bestScores, Mat challenger, int challengerIndex) {
        Mat newMinMask = new Mat(minIndices.size(), minIndices.type());
        cOmpare(challenger, bestScores, newMinMask, CMP_LT);
        minIndices.setTo(new Mat(new Scalar(challengerIndex)), newMinMask);
        challenger.copyTo(bestScores, newMinMask);
    }

    /**
     * Applies a 2D mask to all channels of a 3D Mat, setting masked pixels to zero.
     *
     * <p>Uses a single-channel mask to zero out pixels across all channels of the input Mat. This
     * is useful when you want to apply the same mask pattern to all color channels.
     *
     * <p>Note: Despite the method name suggesting replacement, this actually zeros out masked
     * pixels rather than replacing the mask with the mat.
     *
     * @param mask single-channel mask (255 = set to zero, 0 = keep original)
     * @param mat 3-channel Mat to be masked
     * @return new Mat with masked pixels set to zero in all channels
     */
    public Mat replace2DmaskWith3Dmat(Mat mask, Mat mat) {
        MatVector channels = new MatVector(3);
        split(mat, channels);
        for (int i = 0; i < 3; i++) {
            channels.get(i).setTo(new Mat(new Scalar(0)), mask);
        }
        Mat merged = new Mat();
        merge(channels, merged);
        return merged;
    }

    /**
     * Tracks maximum values and their source indices across multiple images.
     *
     * <p>Updates running maximum values by comparing with a challenger image. When challenger
     * pixels are greater than current maximum, both the maximum values and their source indices are
     * updated.
     *
     * <p>Important: The comment mentions "majority of channels" but the implementation updates each
     * channel independently. Each pixel/channel combination is evaluated separately.
     *
     * <p>Processing:
     *
     * <ol>
     *   <li>Compare each channel: challenger > maxValues
     *   <li>Update maxValues where challenger is greater
     *   <li>Update indices to scalar value where challenger wins
     * </ol>
     *
     * <p>Use case: Finding the brightest or highest-scoring image at each pixel location across a
     * set of images.
     *
     * @param maxValues Mat containing current maximum values (updated in-place)
     * @param challenger Mat with new values to compare
     * @param indices Mat storing the index of the source image for each maximum (updated in-place)
     * @param scalar index identifier for the challenger image
     */
    public void getIndicesOfMax(Mat maxValues, Mat challenger, Mat indices, int scalar) {
        try (Mat challengerWinsMask = new Mat(maxValues.size(), CV_8UC3);
                Mat scalarMat =
                        new Mat(maxValues.size(), CV_8UC3, new Scalar(scalar, scalar, scalar, 0))) {
            cOmpare(
                    maxValues,
                    challenger,
                    challengerWinsMask,
                    CMP_LT); // mask = challenger > maxValues
            challenger.copyTo(maxValues, challengerWinsMask);
            scalarMat.copyTo(indices, challengerWinsMask);
        }
    }

    /**
     * Performs channel-wise range checking, creating a 3-channel mask.
     *
     * <p>Unlike OpenCV's inRange which requires all channels to be within range for a pixel to be
     * selected, this method checks each channel independently, creating separate masks per channel.
     *
     * <p>Differences from OpenCV inRange:
     *
     * <ul>
     *   <li>OpenCV: Single-channel output, all channels must match
     *   <li>This method: 3-channel output, each channel tested independently
     * </ul>
     *
     * <p>Output format:
     *
     * <ul>
     *   <li>255: Value within range [lowerb, upperb]
     *   <li>0: Value outside range
     *   <li>Each channel evaluated separately
     * </ul>
     *
     * <p>Use case: Color filtering where different thresholds or criteria apply to different
     * channels.
     *
     * @param src source 3-channel Mat
     * @param dst destination 3-channel mask (allocated if needed)
     * @param lowerb inclusive lower bound for all channels
     * @param upperb inclusive upper bound for all channels
     */
    public void inrange(Mat src, Mat dst, int lowerb, int upperb) {
        MatVector channels = new MatVector(3);
        split(src, channels);
        MatVector masks = new MatVector(3);
        for (int i = 0; i < 3; i++) {
            Mat mask = new Mat(src.size(), CV_8UC1);
            inRange(
                    channels.get(i),
                    new Mat(new Scalar(lowerb)),
                    new Mat(new Scalar(upperb)),
                    mask);
            masks.put(i, mask);
        }
        merge(masks, dst);
    }

    /**
     * Filters an index Mat to retain only specified index values.
     *
     * <p>Creates a filtered version of an index Mat where only pixels with indices in the specified
     * set are preserved. Other pixels are set to zero.
     *
     * <p>Algorithm:
     *
     * <ol>
     *   <li>For each index in the keep set, create a mask where that index appears
     *   <li>Combine all masks with bitwise OR
     *   <li>Apply combined mask to original indices
     * </ol>
     *
     * <p>Example: If indices contains values [0,1,2,3] and indicesToKeep is {1,3}, the result will
     * have values [0,1,0,3] where 0 replaces indices 0 and 2.
     *
     * <p>Note: Contains an empty Report.println() call that appears to be leftover debug code.
     *
     * @param indices Mat containing index values to filter
     * @param indicesToKeep set of index values to preserve
     * @return new Mat with only specified indices, others set to 0
     */
    public Mat getMatWithOnlyTheseIndices(Mat indices, Set<Integer> indicesToKeep) {
        Mat mask = new Mat(indices.size(), indices.type());
        for (int i : indicesToKeep) {
            Mat mask2 = new Mat(indices.size(), indices.type());
            inrange(indices, mask2, i, i);
            bitwise_or(mask, mask2, mask);
        }
        // the mask will have 255 for the indices to keep and 0 for the others
        Mat onlyIndicesToKeep = new Mat(indices.size(), indices.type());
        bitwise_and(indices, mask, onlyIndicesToKeep);
        return onlyIndicesToKeep;
    }

    /**
     * Counts non-zero pixels in each channel separately.
     *
     * <p>Splits the Mat and counts non-zero values in each channel independently, returning a list
     * of counts.
     *
     * <p>Use cases:
     *
     * <ul>
     *   <li>Measuring channel activity or presence
     *   <li>Calculating per-channel mask coverage
     *   <li>Validating channel-specific thresholds
     * </ul>
     *
     * @param mat multi-channel Mat to analyze
     * @return list of non-zero counts, one per channel
     */
    public List<Integer> cOuntNonZero(Mat mat) {
        MatVector matVector = sPlit(mat);
        List<Integer> counts = new ArrayList<>();
        for (Mat m : matVector.get()) {
            counts.add(countNonZero(m));
        }
        return counts;
    }

    /**
     * Returns the maximum non-zero count across all channels.
     *
     * <p>Finds which channel has the most non-zero pixels, useful for determining the dominant or
     * most active channel.
     *
     * @param mat multi-channel Mat to analyze
     * @return maximum count of non-zero pixels among all channels
     */
    public int getMaxNonZeroCellsByChannel(Mat mat) {
        List<Integer> counts = cOuntNonZero(mat);
        return Collections.max(counts);
    }

    /**
     * Performs channel-wise bitwise AND operation on two multi-channel Mats.
     *
     * <p>Applies bitwise AND to corresponding channels independently, preserving the multi-channel
     * structure. Handles different channel counts by using the minimum.
     *
     * <p>Error handling: Prints dimension mismatches to console but continues processing (may cause
     * runtime errors if dimensions don't match).
     *
     * <p>Performance note: Pre-allocates result channels by splitting a new Mat, which may be
     * inefficient.
     *
     * @param mat1 first multi-channel Mat
     * @param mat2 second multi-channel Mat
     * @return new Mat with channel-wise AND results
     */
    public Mat bItwise_and(Mat mat1, Mat mat2) {
        MatVector vec1 = sPlit(mat1);
        MatVector vec2 = sPlit(mat2);
        MatVector andVec = sPlit(new Mat(mat1.size(), mat1.type()));
        for (int i = 0; i < Math.min(vec1.size(), vec2.size()); i++) {
            if (vec1.get(i).rows() != vec2.get(i).rows()) { // print out mismatch. program will end.
                System.out.println("rows: " + vec1.get(i).rows() + " " + vec2.get(i).rows());
                System.out.println("cols: " + vec1.get(i).cols() + " " + vec2.get(i).cols());
            }
            bitwise_and(vec1.get(i), vec2.get(i), andVec.get(i));
        }
        return mErge(andVec);
    }

    /**
     * Performs channel-wise bitwise OR operation on two multi-channel Mats.
     *
     * <p>Similar to bItwise_and but applies OR operation, useful for combining masks or union
     * operations on multi-channel data.
     *
     * <p>Implementation notes:
     *
     * <ul>
     *   <li>Processes minimum number of channels if counts differ
     *   <li>Prints dimension mismatches but continues (potential error source)
     *   <li>Returns new Mat with OR results
     * </ul>
     *
     * @param mat1 first multi-channel Mat
     * @param mat2 second multi-channel Mat
     * @return new Mat with channel-wise OR results
     */
    public Mat bItwise_or(Mat mat1, Mat mat2) {
        MatVector vec1 = sPlit(mat1);
        MatVector vec2 = sPlit(mat2);
        MatVector vec = sPlit(new Mat(mat1.size(), mat1.type()));
        for (int i = 0; i < Math.min(vec1.size(), vec2.size()); i++) {
            if (vec1.get(i).rows() != vec2.get(i).rows()) { // print out mismatch. program will end.
                System.out.println("rows: " + vec1.get(i).rows() + " " + vec2.get(i).rows());
                System.out.println("cols: " + vec1.get(i).cols() + " " + vec2.get(i).cols());
            }
            bitwise_or(vec1.get(i), vec2.get(i), vec.get(i));
        }
        return mErge(vec);
    }

    /**
     * Performs channel-wise bitwise NOT operation on a multi-channel Mat.
     *
     * <p>Inverts all bits in each channel independently, preserving the multi-channel structure.
     *
     * <p>Use cases:
     *
     * <ul>
     *   <li>Inverting multi-channel masks
     *   <li>Creating negative images
     *   <li>Flipping binary channel data
     * </ul>
     *
     * @param mat1 multi-channel Mat to invert
     * @return new Mat with inverted channels
     */
    public Mat bItwise_not(Mat mat1) {
        MatVector vec1 = sPlit(mat1);
        MatVector vec = sPlit(new Mat(mat1.size(), mat1.type()));
        for (int i = 0; i < vec1.get().length; i++) {
            bitwise_not(vec1.get(i), vec.get(i));
        }
        return mErge(vec);
    }

    /**
     * Creates a solid color Mat of specified size.
     *
     * <p>Utility method for creating uniform color backgrounds or overlays. The color is specified
     * as a BGR Scalar.
     *
     * @param size dimensions of the Mat to create
     * @param colorScalar BGR color values (4th value ignored)
     * @return new 3-channel Mat filled with the specified color
     */
    public static Mat createColorMat(Size size, Scalar colorScalar) {
        return new Mat(size, CV_8UC3, colorScalar);
    }

    /**
     * Overlays a solid color onto masked regions of an image.
     *
     * <p>Replaces pixels in the original image with the specified color wherever the mask value is
     * 255. The operation modifies the original Mat in-place.
     *
     * <p>Common uses:
     *
     * <ul>
     *   <li>Highlighting detected regions
     *   <li>Visualizing segmentation results
     *   <li>Creating color overlays for analysis
     * </ul>
     *
     * @param original the base Mat to modify (modified in-place)
     * @param mask binary mask (255 = apply color, 0 = keep original)
     * @param colorToAdd BGR color to apply in masked regions
     */
    public void addColorToMat(Mat original, Mat mask, Scalar colorToAdd) {
        Mat colorMat = createColorMat(original.size(), colorToAdd);
        colorMat.copyTo(original, mask);
    }

    /**
     * Creates a 3x3 3-channel Mat with identical values in all channels.
     *
     * <p>Convenience method for creating small test matrices where all channels have the same
     * pattern. Limited to 9 values (3x3 matrix).
     *
     * <p>Example: makeMat3D(1,2,3,4,5,6,7,8,9) creates a 3x3 BGR Mat where each channel contains
     * the values arranged in a 3x3 grid.
     *
     * @param values up to 9 values to fill the 3x3 matrix
     * @return 3-channel 3x3 Mat with identical channels
     */
    public Mat makeMat3D(short... values) {
        Mat channel1 = MatrixUtilities.makeMat(values);
        Mat channel2 = MatrixUtilities.makeMat(values);
        Mat channel3 = MatrixUtilities.makeMat(values);
        MatVector matVector = new MatVector(channel1, channel2, channel3);
        return mErge(matVector);
    }

    /**
     * Creates a 3-channel Mat with different values per channel.
     *
     * <p>Allows creation of test matrices where each channel has distinct values. Arrays should be
     * the same length (up to 9 values for 3x3).
     *
     * @param channel1 values for first channel (B in BGR)
     * @param channel2 values for second channel (G in BGR)
     * @param channel3 values for third channel (R in BGR)
     * @return 3-channel Mat with specified channel values
     */
    public Mat makeMat3D(short[] channel1, short[] channel2, short[] channel3) {
        Mat ch1 = MatrixUtilities.makeMat(channel1);
        Mat ch2 = MatrixUtilities.makeMat(channel2);
        Mat ch3 = MatrixUtilities.makeMat(channel3);
        MatVector matVector = new MatVector(ch1, ch2, ch3);
        return mErge(matVector);
    }

    /**
     * Creates a test Pattern object from an array of values.
     *
     * <p>Convenience method for unit testing that creates a Pattern with identical values in all
     * channels. Useful for creating simple test patterns programmatically.
     *
     * @param values array of values to create pattern from (max 9)
     * @return Pattern object containing the 3-channel test image
     */
    public Pattern makeTestPattern(short[] values) {
        Mat mat = makeMat3D(values);
        BufferedImage bufferedImage = bufferedImageOps.convert(mat);
        return new Pattern(bufferedImage);
    }
}
