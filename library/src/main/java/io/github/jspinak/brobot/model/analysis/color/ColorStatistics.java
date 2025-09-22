package io.github.jspinak.brobot.model.analysis.color;

import static io.github.jspinak.brobot.model.analysis.color.ColorSchema.ColorValue.*;

import java.util.Map;

import org.bytedeco.opencv.opencv_core.Scalar;

// Removed old logging import: 
import lombok.Getter;

/**
 * Statistical profile mapping color channels to their values for a specific statistic.
 *
 * <p>ColorStatistics represents a snapshot of a single statistical measure (min, max, mean, or
 * stddev) across all channels of a color space. Unlike {@link ColorSchema} which contains all
 * statistics for each channel, this class focuses on one statistic across all channels, providing a
 * color "fingerprint" for that measure.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Represents one statistic (e.g., MEAN) for all color channels
 *   <li>Maps each ColorValue (HUE, SATURATION, etc.) to its statistical value
 *   <li>Enables color comparison based on specific statistical properties
 *   <li>Supports conversion to OpenCV Scalar for HSV operations
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Creating mean color representations for matching
 *   <li>Defining color ranges using min/max profiles
 *   <li>Comparing colors based on specific statistics
 *   <li>Generating color masks from statistical boundaries
 * </ul>
 *
 * <p>Example: A MEAN ColorStatistics for HSV might contain: {HUE: 120.5, SATURATION: 200.0, VALUE:
 * 180.0}, representing the average color of an analyzed region.
 *
 * @see ColorSchema#getColorStatistics(ColorInfo.ColorStat)
 * @see ColorInfo.ColorStat
 * @see ColorSchema.ColorValue
 */
@Getter
public class ColorStatistics {

    // ColorValue = Hue, Saturation, Value, etc.
    private Map<ColorSchema.ColorValue, Double> means;
    // ColorStat = min, max, mean, stddev
    private ColorInfo.ColorStat colorStat;

    /**
     * Creates a new ColorStatistics with the specified statistic type and channel values.
     *
     * @param colorStat the type of statistic represented (MIN, MAX, MEAN, or STDDEV)
     * @param stats map of color channels to their statistical values
     */
    public ColorStatistics(
            ColorInfo.ColorStat colorStat, Map<ColorSchema.ColorValue, Double> stats) {
        this.colorStat = colorStat;
        this.means = stats;
    }

    /**
     * Retrieves the statistical value for a specific color channel.
     *
     * @param colorValue the color channel to query (e.g., HUE, BLUE)
     * @return the statistical value for the channel, or null if not present
     */
    public double getStat(ColorSchema.ColorValue colorValue) {
        return means.get(colorValue);
    }

    /**
     * Converts this profile to an OpenCV Scalar for HSV operations.
     *
     * <p>Creates a 4-channel Scalar with HSV values and zero alpha. Only valid for profiles
     * containing HSV color values.
     *
     * @return Scalar with format (H, S, V, 0)
     * @throws NullPointerException if HSV values are not present in this profile
     */
    public Scalar getMeanScalarHSV() {
        return new Scalar(means.get(HUE), means.get(SATURATION), means.get(VALUE), 0);
    }

    /**
     * Prints formatted profile information to the report log.
     *
     * <p>Outputs the statistic type followed by each channel's value. Format: "color stat:
     * [STAT_TYPE]" followed by "[CHANNEL] = [VALUE]"
     *
     * <p>Side effects: Outputs to the Report logging system
     */
    public void print() {
        for (ColorSchema.ColorValue colorValue : means.keySet()) {
        }
    }
}
