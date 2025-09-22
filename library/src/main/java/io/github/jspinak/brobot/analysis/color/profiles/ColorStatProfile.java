package io.github.jspinak.brobot.analysis.color.profiles;

import static io.github.jspinak.brobot.model.analysis.color.ColorSchema.ColorValue.*;

import java.util.Map;

import org.bytedeco.opencv.opencv_core.Scalar;

import io.github.jspinak.brobot.model.analysis.color.ColorInfo;
import io.github.jspinak.brobot.model.analysis.color.ColorSchema;
// Removed old logging import: 
import lombok.Getter;

/**
 * Represents a statistical color profile for a specific statistic type in a color space.
 *
 * <p>This class encapsulates color statistics (such as mean, min, max, or standard deviation) for
 * each channel of a color space. Despite the field name 'means', it can represent any statistical
 * measure as specified by the ColorStat type. The profile provides a quantitative description of
 * color characteristics that can be used for comparison and matching.
 *
 * <p>Common uses include:
 *
 * <ul>
 *   <li>Storing mean color values for color-based matching
 *   <li>Defining color ranges using min/max statistics
 *   <li>Measuring color variance with standard deviation
 *   <li>Creating color signatures for state identification
 * </ul>
 *
 * @see ColorInfo.ColorStat
 * @see ColorSchema.ColorValue
 */
@Getter
public class ColorStatProfile {

    /** Maps color channels (Hue, Saturation, Value, etc.) to their statistical values */
    private Map<ColorSchema.ColorValue, Double> means;

    /** The type of statistic represented (min, max, mean, stddev) */
    private ColorInfo.ColorStat colorStat;

    /**
     * Constructs a ColorStatProfile with the specified statistic type and values.
     *
     * @param colorStat The type of color statistic (e.g., MEAN, MIN, MAX, STDDEV)
     * @param stats Map of color channel values for the specified statistic
     */
    public ColorStatProfile(
            ColorInfo.ColorStat colorStat, Map<ColorSchema.ColorValue, Double> stats) {
        this.colorStat = colorStat;
        this.means = stats;
    }

    /**
     * Retrieves the statistical value for a specific color channel.
     *
     * @param colorValue The color channel to retrieve (e.g., HUE, SATURATION, VALUE)
     * @return The statistical value for the specified channel
     */
    public double getStat(ColorSchema.ColorValue colorValue) {
        return means.get(colorValue);
    }

    /**
     * Converts the color statistics to an OpenCV Scalar in HSV format.
     *
     * <p>This method assumes the profile contains HSV values and creates a 4-channel Scalar with
     * the fourth channel set to 0. This is useful for OpenCV operations that require color values
     * in Scalar format.
     *
     * @return An OpenCV Scalar containing HSV values (H, S, V, 0)
     */
    public Scalar getMeanScalarHSV() {
        return new Scalar(means.get(HUE), means.get(SATURATION), means.get(VALUE), 0);
    }

    /**
     * Prints the color profile statistics to the report log.
     *
     * <p>Outputs the statistic type and all channel values in a formatted manner for debugging and
     * analysis purposes.
     */
    public void print() {
        for (ColorSchema.ColorValue colorValue : means.keySet()) {
        }
    }
}
