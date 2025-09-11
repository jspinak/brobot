package io.github.jspinak.brobot.analysis.color;

import static org.bytedeco.opencv.global.opencv_core.*;

import java.util.Arrays;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.internal.find.pixel.PixelScoreCalculator;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.color.ColorInfo;
import io.github.jspinak.brobot.model.element.Region;

/**
 * Calculates distance matrices for color-based pattern matching.
 *
 * <p>DistanceMatrixCalculator provides various distance calculation methods for comparing pixel
 * colors in scenes against target color profiles. These distance metrics form the foundation for
 * color-based matching in Brobot's vision system.
 *
 * <p>Distance calculation methods:
 *
 * <ul>
 *   <li><b>Absolute distance</b>: Direct channel-wise difference
 *   <li><b>Relative distance</b>: Signed distance maintaining direction
 *   <li><b>Range-based distance</b>: Distance outside min/max boundaries
 *   <li><b>Nearest boundary</b>: Minimum distance to any color boundary
 * </ul>
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Supports both BGR and HSV color spaces
 *   <li>Multi-channel distance calculations
 *   <li>Region masking for targeted analysis
 *   <li>Statistical target selection (mean, min, max, etc.)
 * </ul>
 *
 * <p>The distance matrices produced by this class are typically used by {@link
 * PixelScoreCalculator} to generate matching scores and by other analysis components for
 * color-based segmentation and filtering.
 *
 * @see ColorCluster
 * @see ColorAnalysis
 * @see PixelScoreCalculator
 */
@Component
public class DistanceMatrixCalculator {

    /**
     * Calculates per-pixel absolute distance to a target color.
     *
     * <p>Computes channel-wise absolute differences between scene pixels and the specified
     * statistical target from the color cluster. The result is a multi-channel Mat where each
     * channel contains distances for that color component.
     *
     * <p>Side effects: The input scene is cloned and not modified. The target color Mat from
     * colorCluster is converted to CV_64F internally.
     *
     * @param scene the image to analyze (not modified)
     * @param colorSchemaName the color space to use (BGR or HSV)
     * @param colorCluster contains the target color statistics
     * @param colorStat the statistic to use as target (MIN, MAX, MEAN, or STDDEV)
     * @return Mat with per-pixel absolute distances (CV_64FC3 format)
     */
    public Mat getAbsDist(
            Mat scene,
            ColorCluster.ColorSchemaName colorSchemaName,
            ColorCluster colorCluster,
            ColorInfo.ColorStat colorStat) {
        Mat targetColor = colorCluster.getMat(colorSchemaName, colorStat, scene.size());
        targetColor.convertTo(
                targetColor,
                CV_64F); // convert to double; convertTo() does not change the # of channels
        Mat scene64 = scene.clone();
        scene64.convertTo(scene64, CV_64FC3);
        Mat dist = new Mat(scene.size(), CV_64FC3);
        absdiff(scene64, targetColor, dist);
        return dist;
    }

    /**
     * Calculates per-pixel signed distance to a target color.
     *
     * <p>Unlike absolute distance, this maintains directional information, with positive values
     * indicating pixels brighter than the target and negative values indicating darker pixels.
     * Useful for determining which side of a threshold pixels fall on.
     *
     * @param scene the image to analyze
     * @param colorSchemaName the color space to use (BGR or HSV)
     * @param colorCluster contains the target color statistics
     * @param colorStat the statistic to use as target
     * @return Mat with signed distances (CV_64FC format)
     */
    public Mat getRelativeDist(
            Mat scene,
            ColorCluster.ColorSchemaName colorSchemaName,
            ColorCluster colorCluster,
            ColorInfo.ColorStat colorStat) {
        Mat targetColor = colorCluster.getMat(colorSchemaName, colorStat, scene.size());
        Mat dist = new Mat(scene.size(), CV_64FC(scene.channels())); // CV_16SC(scene.channels())
        subtract(scene, targetColor, dist);
        return dist;
    }

    /**
     * Applies a region mask by setting values outside the region.
     *
     * <p>Modifies the input Mat by setting all pixels outside the specified region to a fixed
     * value. This is typically used to exclude areas from analysis by setting them to a high
     * distance value.
     *
     * <p>Side effects: The input mat is modified in-place.
     *
     * @param mat the Mat to modify (modified in-place)
     * @param regionAsMask the region to preserve
     * @param valueOutsideMask the value to set outside the region (typically 255)
     * @return the modified Mat (same reference as input)
     */
    public Mat setOutsideMask(Mat mat, Region regionAsMask, double valueOutsideMask) {
        Mat maskBase = new Mat(mat.size(), 0, new Scalar(valueOutsideMask)); // all cells 255
        Mat regionMask =
                new Mat(
                        maskBase,
                        regionAsMask.getJavaCVRect()); // maskBase with the searchRegion as roi
        bitwise_not(regionMask, regionMask); // now, maskBase has 255 everywhere outside the Mask
        bitwise_or(
                mat,
                regionMask,
                mat); // the values inside the mask are kept. the values outside are set to 255.
        return mat;
    }

    /**
     * Calculates distance outside the min/max color range.
     *
     * <p>For each pixel, computes how far it falls outside the acceptable color range defined by
     * the cluster's min and max values. Pixels within the range have distance 0, while out-of-range
     * pixels have positive distances indicating how far they exceed the boundaries.
     *
     * <p>The initial distance matrices are initialized to 255 to ensure proper subtraction behavior
     * for out-of-range detection.
     *
     * @param scene the image to analyze
     * @param colorSchemaName the color space to use
     * @param colorCluster defines the min/max color range
     * @return Mat with combined out-of-range distances (CV_64FC format)
     */
    public Mat getDistanceBelowMinAndAboveMax(
            Mat scene, ColorCluster.ColorSchemaName colorSchemaName, ColorCluster colorCluster) {
        Mat targetColorMin =
                colorCluster.getMat(colorSchemaName, ColorInfo.ColorStat.MIN, scene.size());
        Mat distBelowMin =
                new Mat(
                        scene.size(),
                        CV_64FC(scene.channels()),
                        new Scalar(255)); // CV_8UC(scene.channels())
        subtract(scene, targetColorMin, distBelowMin, new Mat(), 0);
        Mat targetColorMax =
                colorCluster.getMat(colorSchemaName, ColorInfo.ColorStat.MAX, scene.size());
        Mat distAboveMax = new Mat(scene.size(), CV_64FC(scene.channels()), new Scalar(255));
        subtract(targetColorMax, scene, distAboveMax, new Mat(), 0);
        Mat combinedDist = new Mat(scene.size(), CV_64FC(scene.channels()));
        add(distBelowMin, distAboveMax, combinedDist);
        return combinedDist;
    }

    /**
     * Finds the minimum signed distance to multiple color targets.
     *
     * <p>Calculates signed distances to each specified color statistic and returns the value with
     * the smallest absolute distance while preserving its sign. This is useful for finding which
     * boundary (min or max) a pixel is closest to.
     *
     * <p>Example: For min=20, max=60, a pixel value of 10 gives distances of -10 (to min) and -50
     * (to max). The result is -10 since abs(-10) is less than abs(-50).
     *
     * @param scene the image to analyze
     * @param colorSchemaName the color space to use
     * @param colorCluster contains the target color statistics
     * @param colorStats variable number of statistics to compare (e.g., MIN, MAX)
     * @return Mat with signed distance to nearest target
     */
    public Mat getDistToNearest(
            Mat scene,
            ColorCluster.ColorSchemaName colorSchemaName,
            ColorCluster colorCluster,
            ColorInfo.ColorStat... colorStats) {
        MatVector relDist = new MatVector();
        Arrays.stream(colorStats)
                .forEach(
                        cs ->
                                relDist.put(
                                        getRelativeDist(scene, colorSchemaName, colorCluster, cs)));
        return minAbsAsOriginalValue(relDist);
    }

    /**
     * Finds the minimum absolute value while preserving the original sign.
     *
     * <p>Iterates through a vector of Mats and selects the value with the smallest absolute
     * magnitude at each pixel position, but returns the original signed value rather than the
     * absolute value.
     *
     * @param matVector vector of Mats to compare
     * @return Mat containing original values with minimum absolute magnitude
     */
    private Mat minAbsAsOriginalValue(MatVector matVector) {
        if (matVector.empty()) return new Mat();
        Mat minAbsOrg =
                matVector
                        .get(0)
                        .clone(); // this variable holds the original value of the minimum absolute
        // value
        for (int i = 1; i < matVector.size(); i++) {
            Mat abs1 = new Mat();
            absdiff(minAbsOrg, new Mat(new Scalar(0)), abs1); // absvalue
            Mat abs2 = new Mat();
            absdiff(matVector.get(i), new Mat(new Scalar(0)), abs2); // absvalue
            Mat abs2Smaller = new Mat();
            compare(abs2, abs1, abs2Smaller, CMP_LT); // abs2Larger is 255 where mat1 <= mat2
            minAbsOrg.setTo(
                    abs2,
                    abs2Smaller); // set the minAbsOrg to the value of abs2 where abs2Smaller is 255
        }
        return minAbsOrg;
    }
}
