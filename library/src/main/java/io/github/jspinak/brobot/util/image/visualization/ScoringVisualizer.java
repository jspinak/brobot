package io.github.jspinak.brobot.util.image.visualization;

import io.github.jspinak.brobot.model.analysis.color.ColorSchema;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static io.github.jspinak.brobot.model.analysis.color.ColorInfo.ColorStat.MEAN;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * Visualization component for color matching scores and analysis.
 * <p>
 * This class provides utilities for visualizing color matching results,
 * particularly for debugging and understanding how color schemas match
 * against individual pixels. It creates visual representations showing
 * both the target pixel color and the mean color from a schema.
 * <p>
 * Key features:
 * <ul>
 * <li>Side-by-side comparison of pixel and mean colors</li>
 * <li>Automatic color space conversion (HSV to BGR for display)</li>
 * <li>Text labeling on generated visualizations</li>
 * <li>Resizing for consistent display dimensions</li>
 * <li>Integration with MatVisualize for output handling</li>
 * </ul>
 * <p>
 * Visualization format:
 * <ul>
 * <li>Fixed size blocks (100x100 pixels by default)</li>
 * <li>Horizontal layout with 5-pixel spacing</li>
 * <li>Text labels at bottom of each block</li>
 * <li>BGR color space for accurate display</li>
 * </ul>
 * <p>
 * Use cases:
 * <ul>
 * <li>Debugging color-based object detection</li>
 * <li>Analyzing why certain pixels match/don't match schemas</li>
 * <li>Visualizing color schema parameters</li>
 * <li>Creating documentation of color matching behavior</li>
 * </ul>
 * <p>
 * Thread safety: Not thread-safe due to mutable size field.
 *
 * @see ColorSchema
 * @see MatrixVisualizer
 * @see MatBuilder
 */
@Component
public class ScoringVisualizer {

    private MatrixVisualizer matVisualize;

    /**
     * Default size for color blocks in visualizations (pixels).
     * Both width and height use this value.
     */
    private int size = 100;

    public ScoringVisualizer(MatrixVisualizer matVisualize) {
        this.matVisualize = matVisualize;
    }

    /**
     * Creates a BGR visualization of the mean color from a color schema.
     * <p>
     * Extracts the mean color from the schema, converts it from HSV to BGR
     * for proper display, and adds a "Mean" label. The method includes
     * debug output showing the HSV values and Mat information.
     * <p>
     * Processing steps:
     * <ol>
     * <li>Extract mean color as HSV Mat (size x size)</li>
     * <li>Log HSV values for debugging</li>
     * <li>Convert HSV to BGR for display</li>
     * <li>Add "Mean" text label</li>
     * </ol>
     * <p>
     * Side effects:
     * <ul>
     * <li>Prints partial Mat content to console</li>
     * <li>Reports mean color statistics</li>
     * <li>Logs Mat info for debugging</li>
     * </ul>
     *
     * @param colorSchema the color schema containing mean color information
     * @return BGR Mat of size x size pixels with "Mean" label
     */
    public Mat getMeanBGR(ColorSchema colorSchema) {
        Mat hsvMean = colorSchema.getMat(MEAN, new Size(size,size));
        MatrixUtilities.printPartOfMat(hsvMean, 5, 5,"hsvMean");
        List.of(colorSchema.getColorStats(MEAN)).forEach(dbl -> ConsoleReporter.println("mean: " + Arrays.toString(dbl)));
        MatrixUtilities.info(hsvMean, "hsvMean");
        cvtColor(hsvMean, hsvMean, COLOR_HSV2BGR);
        addText(hsvMean, "Mean");
        return hsvMean;
    }

    /**
     * Converts and resizes an HSV pixel to BGR format for visualization.
     * <p>
     * Takes a single HSV pixel (or small Mat), converts it to BGR for
     * proper color display, and resizes it to the standard visualization
     * size. Adds a "Pixel" label for identification.
     * <p>
     * Processing steps:
     * <ol>
     * <li>Convert HSV to BGR color space</li>
     * <li>Resize to size x size using area interpolation</li>
     * <li>Log Mat info for debugging</li>
     * <li>Add "Pixel" text label</li>
     * </ol>
     * <p>
     * Note: INTER_AREA interpolation is used for downsampling as it
     * provides better visual quality for solid color blocks.
     *
     * @param hsvPixel the HSV color Mat to visualize (any size)
     * @return BGR Mat of size x size pixels with "Pixel" label
     */
    public Mat getPixelBGR(Mat hsvPixel) {
        Mat bgrPixel = new Mat();
        cvtColor(hsvPixel, bgrPixel, COLOR_HSV2BGR);
        Mat bgrPixelResized = new Mat();
        resize(bgrPixel, bgrPixelResized, new Size(size,size), 0, 0, INTER_AREA);
        MatrixUtilities.info(bgrPixel, "bgrPixel");
        addText(bgrPixelResized, "Pixel");
        return bgrPixelResized;
    }

    /**
     * Creates and saves a side-by-side visualization of a pixel and schema mean.
     * <p>
     * Generates a horizontal comparison showing the actual pixel color
     * alongside the mean color from the schema. This visualization helps
     * understand how closely a pixel matches the expected color range.
     * <p>
     * Layout:
     * <ul>
     * <li>Left: Actual pixel color (labeled "Pixel")</li>
     * <li>Center: 5-pixel spacing</li>
     * <li>Right: Schema mean color (labeled "Mean")</li>
     * </ul>
     * <p>
     * The combined image is saved to history with the filename "pixelAndMean"
     * for later analysis or debugging.
     *
     * @param hsvPixel the HSV pixel to compare
     * @param colorSchema the color schema containing the mean to compare against
     */
    public void showPixelAndMean(Mat hsvPixel, ColorSchema colorSchema) {
        Mat bgrPixel = getPixelBGR(hsvPixel);
        Mat bgrMean = getMeanBGR(colorSchema);
        Mat pixMean = new MatBuilder()
            .addHorizontalSubmats(bgrPixel, bgrMean)
            .setSpaceBetween(5)
            .build();
        matVisualize.writeMatToHistory(pixMean, "pixelAndMean");
    }

    /**
     * Adds white text label to the bottom of an image Mat.
     * <p>
     * Places text at the bottom-left corner of the Mat using OpenCV's
     * putText function. The text appears in white color with standard
     * font settings.
     * <p>
     * Text properties:
     * <ul>
     * <li>Font: FONT_HERSHEY_SIMPLEX</li>
     * <li>Scale: 1.0</li>
     * <li>Color: White (255, 255, 255)</li>
     * <li>Position: Bottom-left corner</li>
     * </ul>
     * <p>
     * Note: The text may extend beyond Mat boundaries if too long.
     * Consider Mat size when choosing text length.
     *
     * @param mat the Mat to add text to (modified in-place)
     * @param text the text string to add
     */
    public void addText(Mat mat, String text) {
        int fontScale = 1;
        Scalar fontColor = new Scalar(255, 255, 255, 0);
        Point xy = new Point(0, mat.rows());
        putText(mat, text, xy, FONT_HERSHEY_SIMPLEX, fontScale, fontColor);
    }

    /**
     * Displays detailed HSV color matching analysis (not yet implemented).
     * <p>
     * This method is intended to show comprehensive color matching information
     * including scores, distances, and range boundaries. When implemented, it
     * would provide detailed visualization of why a pixel does or doesn't match
     * a color schema.
     * <p>
     * Planned features:
     * <ul>
     * <li>Visual representation of HSV ranges</li>
     * <li>Score and distance metrics display</li>
     * <li>Threshold visualization</li>
     * <li>Color space position indicators</li>
     * </ul>
     *
     * @param hsvPixel the HSV pixel being analyzed
     * @param score the matching score
     * @param distBelowThreshold distance below threshold value
     * @param distToMean distance to the mean color
     * @param meanH mean hue value
     * @param meanS mean saturation value  
     * @param meanV mean value (brightness)
     * @param rangeMinH minimum hue in acceptable range
     * @param rangeMinS minimum saturation in acceptable range
     * @param rangeMinV minimum value in acceptable range
     * @param rangeMaxH maximum hue in acceptable range
     * @param rangeMaxS maximum saturation in acceptable range
     * @param rangeMaxV maximum value in acceptable range
     * @param distOutsideRange distance outside the acceptable range
     */
    public void showPixelHSV(Mat hsvPixel, double score, double distBelowThreshold, double distToMean,
                             double meanH, double meanS, double meanV, double rangeMinH, double rangeMinS, double rangeMinV,
                             double rangeMaxH, double rangeMaxS, double rangeMaxV, double distOutsideRange) {

    }
}
