package io.github.jspinak.brobot.model.analysis.color;

import java.util.Map;
import java.util.TreeMap;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import io.github.jspinak.brobot.util.image.core.MatrixUtilities;

import lombok.Getter;
import lombok.Setter;

/**
 * Abstract color space representation for statistical color matching.
 *
 * <p>ColorSchema serves as the base class for specific color space implementations (BGR and HSV),
 * providing a unified interface for managing color channel statistics. Each schema contains a
 * collection of {@link ColorInfo} objects, one for each color channel in the color space.
 *
 * <p>Architecture:
 *
 * <ul>
 *   <li>Abstract representation of a color space (BGR, HSV, etc.)
 *   <li>Contains ordered ColorInfo objects for each channel
 *   <li>Provides methods for statistical operations across channels
 *   <li>Supports Mat generation for OpenCV operations
 * </ul>
 *
 * <p>Supported color values:
 *
 * <ul>
 *   <li><b>HSV</b>: HUE, SATURATION, VALUE
 *   <li><b>BGR</b>: BLUE, GREEN, RED
 * </ul>
 *
 * <p>Key operations:
 *
 * <ul>
 *   <li>Setting and retrieving channel statistics
 *   <li>Generating OpenCV Mat objects with statistical values
 *   <li>Creating color stat profiles for specific statistics
 *   <li>Formatted output for debugging and analysis
 * </ul>
 *
 * <p>The TreeMap storage ensures consistent ordering of color channels, which is critical for
 * proper Mat generation and channel indexing.
 *
 * @see ColorInfo
 * @see ColorSchemaBGR
 * @see ColorSchemaHSV
 * @see ColorStatistics
 */
@Getter
@Setter
public class ColorSchema {

    public enum ColorValue {
        HUE,
        SATURATION,
        VALUE,
        RED,
        GREEN,
        BLUE
    }

    private Map<ColorValue, ColorInfo> colorInfos =
            new TreeMap<>(); // TreeMap keeps order of insertion

    public ColorSchema(ColorValue... colorValues) {
        for (ColorValue colorValue : colorValues) {
            colorInfos.put(colorValue, new ColorInfo(colorValue));
        }
    }

    /**
     * Sets statistical values for a specific color channel.
     *
     * <p>Updates all statistics (min, max, mean, stddev) for the specified color channel. No
     * operation if the color value doesn't exist in this schema.
     *
     * @param colorValue the color channel to update (e.g., HUE, BLUE)
     * @param min minimum value in the channel
     * @param max maximum value in the channel
     * @param mean average value across all pixels
     * @param stddev standard deviation of values
     */
    public void setValues(
            ColorValue colorValue, double min, double max, double mean, double stddev) {
        if (!colorInfos.containsKey(colorValue)) return;
        colorInfos.get(colorValue).setAll(min, max, mean, stddev);
    }

    /**
     * Sets statistical values for a color channel by index.
     *
     * <p>Updates statistics using the channel's position in the schema. Useful when working with
     * channel data in array form.
     *
     * <p>Side effects: Logs error message if index is out of range
     *
     * @param infosIndex zero-based index of the color channel
     * @param min minimum value in the channel
     * @param max maximum value in the channel
     * @param mean average value across all pixels
     * @param stddev standard deviation of values
     */
    public void setValues(int infosIndex, double min, double max, double mean, double stddev) {
        if (infosIndex < 0 || infosIndex >= colorInfos.size()) {
            return;
        }
        ColorInfo colorInfo = colorInfos.values().toArray(new ColorInfo[0])[infosIndex];
        colorInfo.setAll(min, max, mean, stddev);
    }

    /**
     * Checks if this schema contains a specific color channel.
     *
     * @param colorValue the color channel to check
     * @return true if the channel exists in this schema
     */
    public boolean contains(ColorValue colorValue) {
        return colorInfos.containsKey(colorValue);
    }

    /**
     * Creates a Mat of the colorStat for all ColorValues in this Schema.
     *
     * <p>Generates an OpenCV Mat where each channel contains the specified statistical value for
     * the corresponding color channel. For example, with colorStat = MIN and HSV schema, produces a
     * 3-channel Mat with {min Hue, min Sat, min Val} across channels. All pixels in the same
     * channel have the same value.
     *
     * @param colorStat the statistic to use (MIN, MAX, MEAN, or STDDEV)
     * @param size dimensions of the output Mat
     * @return Mat with statistical values across color channels
     */
    public Mat getMat(ColorInfo.ColorStat colorStat, Size size) {
        return MatrixUtilities.makeMat(size, 16, getColorStats(colorStat));
    }

    /**
     * Retrieves statistical values for all color channels.
     *
     * <p>Returns an array of the specified statistic for each channel in schema order. For example,
     * for BGR with MEAN statistic, returns [mean_blue, mean_green, mean_red].
     *
     * @param colorStat the statistic to retrieve
     * @return array of statistical values in channel order
     */
    public double[] getColorStats(ColorInfo.ColorStat colorStat) {
        return colorInfos.values().stream()
                .mapToDouble(colorInfo -> colorInfo.getStat(colorStat))
                .toArray();
    }

    /**
     * Creates a profile mapping color channels to their statistical values.
     *
     * <p>Generates a {@link ColorStatistics} containing the specified statistic for each color
     * channel. For example, with colorStat = MIN and HSV schema, returns a profile with {HUE:
     * min_hue, SATURATION: min_sat, VALUE: min_val}.
     *
     * @param colorStat the statistic to profile
     * @return ColorStatProfile mapping channels to their statistical values
     * @see ColorStatistics
     */
    public ColorStatistics getColorStatistics(ColorInfo.ColorStat colorStat) {
        Map<ColorValue, Double> colorStatMap = new TreeMap<>();
        for (Map.Entry<ColorValue, ColorInfo> colorInfoEntry : colorInfos.entrySet()) {
            colorStatMap.put(colorInfoEntry.getKey(), colorInfoEntry.getValue().getStat(colorStat));
        }
        return new ColorStatistics(colorStat, colorStatMap);
    }

    /**
     * Prints formatted statistics for all color channels.
     *
     * <p>Outputs detailed statistical information for each channel in the schema. Useful for
     * debugging and color profile analysis.
     *
     * <p>Side effects: Outputs to Report logging system
     */
    public void print() {
        if (colorInfos.isEmpty()) {
            return;
        }
        for (ColorInfo info : colorInfos.values()) {
            info.print();
        }
    }
}
