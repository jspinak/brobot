package io.github.jspinak.brobot.util.image.visualization;

import static io.github.jspinak.brobot.model.analysis.color.ColorSchema.ColorValue.HUE;
import static io.github.jspinak.brobot.model.analysis.color.ColorSchema.ColorValue.SATURATION;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

import java.util.*;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.analysis.color.ColorStatistics;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import io.github.jspinak.brobot.config.core.BrobotProperties;

/**
 * Visualization utilities for OpenCV Mat objects, primarily for debugging and testing.
 *
 * <p>This component provides methods to convert abstract Mat data into visual representations
 * suitable for human interpretation. It specializes in visualizing classification results, score
 * maps, and indexed data using color-coding techniques.
 *
 * <p>Key visualization techniques:
 *
 * <ul>
 *   <li><b>Score visualization</b>: Maps numerical scores to color intensity (brightness)
 *   <li><b>Index visualization</b>: Assigns unique colors to different class indices
 *   <li><b>Hue-based display</b>: Creates color images from single-channel hue data
 *   <li><b>Grayscale expansion</b>: Converts single-channel to 3-channel for saving
 * </ul>
 *
 * <p>Color mapping strategies:
 *
 * <ul>
 *   <li>Scores: Low scores → bright colors, high scores → dark colors (inverted for visibility)
 *   <li>Indices: Automatic hue distribution across HSV spectrum (0-160 range)
 *   <li>Custom: User-provided color maps for specific visualizations
 * </ul>
 *
 * <p>File handling:
 *
 * <ul>
 *   <li>Automatic unique naming with random suffixes
 *   <li>Saves to "history/" directory by default
 *   <li>PNG format for all outputs
 *   <li>Integration with ImageUtils for path management
 * </ul>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Debugging classification algorithms
 *   <li>Visualizing confidence/score maps
 *   <li>Displaying segmentation results
 *   <li>Creating visual logs of processing steps
 * </ul>
 *
 * <p>Thread safety: NOT thread-safe due to mutable uniqueNumber field.
 *
 * @see ImageFileUtilities
 * @see ColorStatistics
 * @see MatrixUtilities
 */
@Component
public class MatrixVisualizer {

    @Autowired
    private BrobotProperties brobotProperties;

    private final ImageFileUtilities imageUtils;

    /**
     * Counter for generating unique filenames (deprecated - now uses Random). Left for potential
     * future use or backwards compatibility.
     */
    int uniqueNumber = 0; // used to make filenames unique

    public MatrixVisualizer(ImageFileUtilities imageUtils) {
        this.imageUtils = imageUtils;
    }

    /**
     * Visualizes score matrices using HSV color mapping with inverted brightness.
     *
     * <p>This method is designed for classification score visualization where lower scores indicate
     * higher confidence. The scores are inverted and mapped to the Value (brightness) channel of
     * HSV, creating a visualization where:
     *
     * <ul>
     *   <li>Low scores (high confidence) → Bright colors
     *   <li>High scores (low confidence) → Dark colors
     * </ul>
     *
     * <p>Color mapping:
     *
     * <ul>
     *   <li>Hue: Fixed from ColorStatProfile
     *   <li>Saturation: Fixed from ColorStatProfile
     *   <li>Value: Inverted score values (bitwise NOT)
     * </ul>
     *
     * <p>Processing steps:
     *
     * <ol>
     *   <li>Extract first channel if multi-channel input
     *   <li>Create constant H and S channels from color profile
     *   <li>Invert scores for V channel (bitwise NOT)
     *   <li>Merge HSV channels and convert to BGR
     *   <li>Save with random suffix to prevent overwrites
     * </ol>
     *
     * <p>Error handling: Returns early with console message if inputs are null/empty.
     *
     * @param toShow the score matrix to visualize; can be single or multi-channel
     * @param color the color profile providing hue and saturation values
     * @param filename base filename for saving (random number appended)
     */
    public void writeScoresHSV(Mat toShow, ColorStatistics color, String filename) {
        if (toShow == null || toShow.empty()) {
            ConsoleReporter.println(
                    "MatrixVisualizer.writeHSV: toShow is null or empty for " + filename);
            return;
        }
        if (color == null) {
            ConsoleReporter.println("MatrixVisualizer.writeHSV: color is null for " + filename);
            return;
        }
        MatVector toShowChannels = new MatVector(toShow.channels());
        color.print();
        Mat ch1 = new Mat(toShow.size(), CV_8UC1, Scalar.all(color.getStat(HUE)));
        Mat ch2 = new Mat(toShow.size(), CV_8UC1, Scalar.all(color.getStat(SATURATION)));
        Mat ch3; // this should be a 2d Mat with scores, but could also be 3d; in this case we
        // take just the first channel
        if (toShow.channels() == 1) {
            ch3 = toShow;
        } else {
            split(toShow, toShowChannels);
            ch3 = toShowChannels.get(0);
        }
        // ch3 represents the score, so we want to invert it onto the value channel
        // this way, low scores are bright and high scores are dark
        bitwise_not(ch3, ch3);
        MatrixUtilities.info(ch1, "ch1");
        MatrixUtilities.info(ch2, "ch2");
        MatrixUtilities.info(ch3, "ch3");
        MatVector matVector = new MatVector(ch1, ch2, ch3);
        Mat hsv = new Mat();
        merge(matVector, hsv);
        hsv.convertTo(hsv, CV_32FC3);
        Mat bgr = new Mat();
        cvtColor(hsv, bgr, COLOR_HSV2BGR);
        imwrite(
                "history/" + filename + new Random().nextInt(1000) + ".png",
                bgr); // write in BGR format
    }

    /**
     * Creates a full HSV image from a single-channel hue Mat.
     *
     * <p>Expands a hue-only Mat into a complete HSV image by adding maximum saturation and value
     * channels. This creates vivid, fully saturated colors based on the input hue values.
     *
     * <p>Channel assignment:
     *
     * <ul>
     *   <li>H (Hue): From input Mat
     *   <li>S (Saturation): 255 (maximum saturation)
     *   <li>V (Value): 255 (maximum brightness)
     * </ul>
     *
     * <p>Use case: Converting hue-based classifications or angle data into colorful visualizations.
     *
     * @param toShow single-channel Mat containing hue values (0-179 in OpenCV)
     * @return 3-channel HSV Mat, or null if input is null/empty
     */
    public Mat getHSVfromHue(Mat toShow) {
        if (toShow == null || toShow.empty()) {
            ConsoleReporter.println("MatrixVisualizer.write2dHueMat: toShow is null or empty.");
            return null;
        }
        MatVector toShowChannels = new MatVector(toShow.channels());
        Mat ch1 = toShow;
        Mat ch2 = new Mat(toShow.size(), CV_8UC1, Scalar.all(255));
        Mat ch3 = new Mat(toShow.size(), CV_8UC1, Scalar.all(255));
        MatVector matVector = new MatVector(ch1, ch2, ch3);
        Mat hsv = new Mat();
        merge(matVector, hsv);
        return hsv;
    }

    /**
     * Converts a single-channel grayscale Mat to 3-channel BGR.
     *
     * <p>Duplicates the single channel across all three BGR channels, creating a grayscale image in
     * BGR format. This is necessary for saving grayscale data in standard image formats that expect
     * 3-channel input.
     *
     * <p>Channel mapping:
     *
     * <ul>
     *   <li>B = G = R = input channel
     * </ul>
     *
     * <p>Note: Despite the method name mentioning "2dHueMat" in the error message, this method
     * handles grayscale conversion, not hue data.
     *
     * @param toShow single-channel grayscale Mat
     * @return 3-channel BGR Mat with identical channels, or null if input is null/empty
     */
    public Mat getBGRfromBW(Mat toShow) {
        if (toShow == null || toShow.empty()) {
            ConsoleReporter.println("MatrixVisualizer.write2dHueMat: toShow is null or empty.");
            return null;
        }
        MatVector toShowChannels = new MatVector(toShow.channels());
        Mat ch1 = toShow;
        Mat ch2 = toShow;
        Mat ch3 = toShow;
        MatVector matVector = new MatVector(ch1, ch2, ch3);
        Mat bgr = new Mat();
        merge(matVector, bgr);
        return bgr;
    }

    /**
     * Converts a hue-only Mat directly to BGR format.
     *
     * <p>Convenience method that combines HSV creation and BGR conversion in a single step. The
     * intermediate HSV image has maximum saturation and value for vivid colors.
     *
     * <p>Processing pipeline:
     *
     * <ol>
     *   <li>Create HSV with S=255, V=255
     *   <li>Convert to 32-bit float for color conversion
     *   <li>Convert HSV to BGR color space
     * </ol>
     *
     * @param toShow single-channel Mat with hue values
     * @return BGR Mat ready for display/saving
     */
    private Mat getBGRfromHue(Mat toShow) {
        Mat hsv = getHSVfromHue(toShow);
        hsv.convertTo(hsv, CV_32FC3);
        Mat bgr = new Mat();
        cvtColor(hsv, bgr, COLOR_HSV2BGR);
        return bgr;
    }

    /**
     * Creates a color visualization of classification indices with automatic color assignment.
     *
     * <p>Converts a matrix of class indices into a colorful BGR image where each unique index
     * (except 0) is assigned a distinct color. Colors are automatically distributed across the hue
     * spectrum based on the number of classes.
     *
     * <p>Color assignment:
     *
     * <ul>
     *   <li>Index 0: Black (reserved for "no class")
     *   <li>Other indices: Evenly distributed hues (0-160 range)
     *   <li>All colors have maximum saturation and value
     * </ul>
     *
     * <p>Example: With 4 classes (indices 1-4), hues would be:
     *
     * <ul>
     *   <li>Index 1: Hue 0 (Red)
     *   <li>Index 2: Hue 40 (Yellow-Orange)
     *   <li>Index 3: Hue 80 (Green)
     *   <li>Index 4: Hue 120 (Cyan)
     * </ul>
     *
     * @param indexMat 2D Mat containing classification indices
     * @return BGR Mat with color-coded visualization
     */
    public Mat getBGRColorMatFromHSV2dIndexMat(Mat indexMat) {
        List<Integer> indices = getIndicesExcluding0(indexMat);
        Map<Integer, Scalar> indexToColor = getHueMap(indices);
        return getBGRColorMatFromHSV2dIndexMat(indexMat, indexToColor);
    }

    /**
     * Creates a color visualization of classification indices with custom color mapping.
     *
     * <p>Similar to the automatic version but allows precise control over the color assigned to
     * each index. Useful when specific colors have semantic meaning or when maintaining consistency
     * across multiple visualizations.
     *
     * <p>Processing steps:
     *
     * <ol>
     *   <li>Extract first channel if multi-channel input
     *   <li>Find all unique indices (excluding 0)
     *   <li>Create masks for each index value
     *   <li>Apply corresponding color from the map
     *   <li>Convert final HSV result to BGR
     * </ol>
     *
     * <p>Performance note: Uses masking operations for each unique index, which may be slow for
     * images with many classes.
     *
     * @param indexMat 2D Mat containing classification indices
     * @param colors map from index values to HSV color Scalars
     * @return BGR Mat with custom color-coded visualization
     */
    public Mat getBGRColorMatFromHSV2dIndexMat(Mat indexMat, Map<Integer, Scalar> colors) {
        Mat indexCh1 = new Mat(indexMat.size(), CV_8UC1);
        if (indexMat.channels() > 1) indexCh1 = MatrixUtilities.getFirstChannel(indexMat);
        List<Integer> indices = getIndicesExcluding0(indexCh1);
        Mat colorMat = new Mat(indexCh1.size(), CV_8UC3, new Scalar(0, 0, 0, 0));
        for (int i = 0; i < indices.size(); i++) {
            int index = indices.get(i);
            if (colors.containsKey(index)) {
                Mat mask = new Mat();
                inRange(indexCh1, new Mat(new Scalar(index)), new Mat(new Scalar(index)), mask);
                Mat color = new Mat(indexCh1.size(), CV_8UC3, colors.get(index));
                color.copyTo(colorMat, mask);
            }
        }
        Mat bgrMat = new Mat();
        cvtColor(colorMat, bgrMat, COLOR_HSV2BGR);
        return bgrMat;
    }

    /**
     * Saves an index visualization with custom colors to the history directory.
     *
     * <p>Creates a color-coded visualization of the index matrix using the provided color map and
     * saves it with a random suffix to prevent filename collisions.
     *
     * <p>Debug output: Prints "hues = " to console (incomplete debug statement).
     *
     * <p>File naming: history/{filename}{random0-999}.png
     *
     * @param mat the index matrix to visualize
     * @param filename base filename for the output
     * @param hues map from index values to HSV colors
     */
    public void writeIndices(Mat mat, String filename, Map<Integer, Scalar> hues) {
        ConsoleReporter.print("hues = ");
        ConsoleReporter.println();
        Mat bgrMat = getBGRColorMatFromHSV2dIndexMat(mat, hues);
        imwrite("history/" + filename + new Random().nextInt(1000) + ".png", bgrMat);
    }

    /**
     * Saves an index visualization with automatic color assignment.
     *
     * <p>Convenience method that creates a visualization with automatically distributed colors and
     * saves it using the standard history path management from ImageUtils.
     *
     * @param indexMat the index matrix to visualize
     * @param filename base filename for the output
     */
    public void writeIndices(Mat indexMat, String filename) {
        writeMatToHistory(getBGRColorMatFromHSV2dIndexMat(indexMat), filename);
    }

    /**
     * Generates a color map for indices using evenly distributed hues.
     *
     * <p>Creates distinct colors for each index by distributing them across the hue spectrum. The
     * range is limited to 0-160 (instead of 0-179) to avoid colors that are too similar to red at
     * both ends of the circular hue scale.
     *
     * <p>Hue distribution examples:
     *
     * <ul>
     *   <li>2 classes: Red (0°) and Green (80°)
     *   <li>5 classes: Steps of 32° from Red to Purple
     *   <li>10 classes: Steps of 16° from Red to Hot Pink
     * </ul>
     *
     * <p>All generated colors have maximum saturation (255) and value (255) for high visibility and
     * distinction.
     *
     * @param indicesWithout0 list of unique indices excluding 0
     * @return map from index to HSV color Scalar, empty if no indices
     */
    private Map<Integer, Scalar> getHueMap(List<Integer> indicesWithout0) {
        if (indicesWithout0.size() == 0) {
            ConsoleReporter.println(
                    "MatrixVisualizer getBGRMat...: Mat does not have valid index values.");
            return new HashMap<>();
        }
        int hueStep = 160 / indicesWithout0.size();
        Map<Integer, Scalar> hues = new HashMap<>();
        for (int i = 0; i < indicesWithout0.size(); i++) {
            hues.put(indicesWithout0.get(i), new Scalar(i * hueStep, 255, 255, 0));
        }
        return hues;
    }

    /**
     * Extracts all unique index values from a Mat, excluding 0.
     *
     * <p>Scans the matrix to find all unique values between the minimum and maximum, excluding 0
     * which is reserved for "no class" or background. This method is optimized to check only values
     * in the min-max range rather than scanning every pixel.
     *
     * <p>Algorithm:
     *
     * <ol>
     *   <li>Find min and max values in first channel
     *   <li>Iterate through range [min, max]
     *   <li>Check if each value exists in the Mat
     *   <li>Add to list if present and not 0
     * </ol>
     *
     * <p>Performance: O(range × pixels) where range = max - min
     *
     * @param indicesMat a Mat containing index values (may be multi-channel)
     * @return sorted list of unique indices excluding 0, empty if error
     */
    private List<Integer> getIndicesExcluding0(Mat indicesMat) {
        List<Integer> indices = new ArrayList<>();
        double[] minMax = MatrixUtilities.getMinMaxOfFirstChannel(indicesMat);
        if (minMax.length <= 1) {
            ConsoleReporter.println("MatrixVisualizer getBGRMat...: min or max is null");
            return indices;
        }
        for (int i = (int) minMax[0]; i <= (int) minMax[1]; i++) {
            if (i != 0 && MatrixUtilities.firstChannelContains(indicesMat, i)) {
                indices.add(i);
            }
        }
        return indices;
    }

    /**
     * Saves a Mat to the history directory with automatic unique naming.
     *
     * <p>Uses ImageUtils to generate a unique filename, preventing overwrites when saving multiple
     * versions of the same visualization. The file is saved in PNG format to the path specified in
     * BrobotProperties.
     *
     * <p>File path: {BrobotProperties history path}{filename}[-{number}].png
     *
     * <p>This is the preferred method for saving visualizations as it ensures unique filenames and
     * consistent directory structure.
     *
     * @param mat the Mat to save (any type/channels)
     * @param filename base filename without extension
     */
    public void writeMatToHistory(Mat mat, String filename) {
        String path = imageUtils.getFreePath(brobotProperties.getScreenshot().getHistoryPath() + filename);
        imwrite(path + ".png", mat);
    }
}
