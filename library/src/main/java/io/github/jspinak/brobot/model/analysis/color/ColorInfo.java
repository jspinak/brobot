package io.github.jspinak.brobot.model.analysis.color;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

/**
 * Statistical color channel information for pattern matching.
 * 
 * <p>ColorInfo encapsulates statistical properties (minimum, maximum, mean, and standard 
 * deviation) for a single color channel within a color space. Each ColorInfo instance 
 * represents one component of a color schema, such as Hue in HSV or Blue in BGR.</p>
 * 
 * <p>Statistical properties stored:
 * <ul>
 *   <li><b>MIN</b>: Minimum value found in the color channel</li>
 *   <li><b>MAX</b>: Maximum value found in the color channel</li>
 *   <li><b>MEAN</b>: Average value across all pixels in the channel</li>
 *   <li><b>STDDEV</b>: Standard deviation indicating color variance</li>
 * </ul>
 * </p>
 * 
 * <p>Role in color matching:
 * <ul>
 *   <li>Defines acceptable color ranges for pattern matching</li>
 *   <li>Enables tolerance-based matching using statistical distributions</li>
 *   <li>Supports multi-channel color analysis when combined in ColorSchema</li>
 *   <li>Provides metrics for color similarity calculations</li>
 * </ul>
 * </p>
 * 
 * <p>Usage patterns:
 * <ul>
 *   <li>Created and populated during color profile analysis</li>
 *   <li>Used by matching algorithms to determine color similarity</li>
 *   <li>Combined with other ColorInfos to form complete color profiles</li>
 *   <li>Enables debugging through formatted statistical output</li>
 * </ul>
 * </p>
 * 
 * <p>Value ranges depend on the color space and channel:
 * <ul>
 *   <li>BGR channels: 0-255</li>
 *   <li>HSV Hue: 0-179 (OpenCV convention)</li>
 *   <li>HSV Saturation/Value: 0-255</li>
 * </ul>
 * </p>
 * 
 * @see ColorSchema
 * @see ColorCluster
 * @see ColorSchema.ColorValue
 */
@Getter
@Setter
public class ColorInfo {

    public enum ColorStat {
        MIN, MAX, MEAN, STDDEV
    }

    private ColorSchema.ColorValue colorValue;

    private Map<ColorStat, Double> stats = new HashMap<>();

    public ColorInfo(ColorSchema.ColorValue colorValue) {
        this.colorValue = colorValue;
    }

    /**
     * Prints formatted statistical information for this color channel.
     * 
     * <p>Output format: "[ColorValue] min.max.mean.stddev = [min] [max] [mean] [stddev]"</p>
     * 
     * <p>Side effects: Outputs to the Report logging system</p>
     */
    public void print() {
        ConsoleReporter.formatln("%s min.max.mean.stddev = %,.0f %,.0f %,.1f %,.1f",colorValue,
                stats.get(ColorStat.MIN), stats.get(ColorStat.MAX), stats.get(ColorStat.MEAN), stats.get(ColorStat.STDDEV));
    }

    /**
     * Sets all statistical values for this color channel.
     * 
     * <p>Updates the internal statistics map with all four statistical measures
     * in a single operation. Typically called during color profile generation.</p>
     * 
     * @param min minimum value found in the color channel
     * @param max maximum value found in the color channel
     * @param mean average value across all pixels
     * @param stdDev standard deviation of color values
     */
    public void setAll(double min, double max, double mean, double stdDev) {
        stats.put(ColorStat.MIN, min);
        stats.put(ColorStat.MAX, max);
        stats.put(ColorStat.MEAN, mean);
        stats.put(ColorStat.STDDEV, stdDev);
    }

    /**
     * Retrieves a specific statistical value for this color channel.
     * 
     * @param colorStat the type of statistic to retrieve (MIN, MAX, MEAN, or STDDEV)
     * @return the statistical value, or null if not set
     */
    public double getStat(ColorStat colorStat) {
        return stats.get(colorStat);
    }

}
