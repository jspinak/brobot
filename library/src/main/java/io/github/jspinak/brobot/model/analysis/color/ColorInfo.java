package io.github.jspinak.brobot.model.analysis.color;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ColorInfo implements Comparable<ColorInfo> {

    public enum ColorStat {
        MIN, MAX, MEAN, STDDEV
    }

    private ColorSchema.ColorValue colorValue;
    @Builder.Default
    private Map<ColorStat, Double> stats = new HashMap<>();
    
    // RGB values
    private int red;
    private int green;
    private int blue;
    
    // HSV values
    private double hue;
    private double saturation;
    private double value;
    
    // Metadata
    private String name;
    private int frequency;
    private int totalPixels;

    public ColorInfo(ColorSchema.ColorValue colorValue) {
        this.colorValue = colorValue;
        this.stats = new HashMap<>();
    }

    /**
     * Prints formatted statistical information for this color channel.
     * 
     * <p>Output format: "[ColorValue] min.max.mean.stddev = [min] [max] [mean] [stddev]"</p>
     * 
     * <p>Side effects: Outputs to the Report logging system</p>
     */
    public void print() {
        if (colorValue != null && stats != null && !stats.isEmpty()) {
            ConsoleReporter.formatln("%s min.max.mean.stddev = %,.0f %,.0f %,.1f %,.1f", colorValue,
                    stats.get(ColorStat.MIN), stats.get(ColorStat.MAX), 
                    stats.get(ColorStat.MEAN), stats.get(ColorStat.STDDEV));
        }
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
        if (stats == null) {
            stats = new HashMap<>();
        }
        stats.put(ColorStat.MIN, min);
        stats.put(ColorStat.MAX, max);
        stats.put(ColorStat.MEAN, mean);
        stats.put(ColorStat.STDDEV, stdDev);
    }

    /**
     * Retrieves a specific statistical value for this color channel.
     * 
     * @param colorStat the type of statistic to retrieve (MIN, MAX, MEAN, or STDDEV)
     * @return the statistical value, or 0.0 if not set
     */
    public double getStat(ColorStat colorStat) {
        if (stats == null) {
            return 0.0;
        }
        return stats.getOrDefault(colorStat, 0.0);
    }
    
    // RGB value setters with clamping
    public void setRed(int red) {
        this.red = Math.max(0, Math.min(255, red));
    }
    
    public void setGreen(int green) {
        this.green = Math.max(0, Math.min(255, green));
    }
    
    public void setBlue(int blue) {
        this.blue = Math.max(0, Math.min(255, blue));
    }
    
    // HSV value setters with validation
    public void setHue(double hue) {
        // Wrap hue to [0, 360)
        this.hue = ((hue % 360) + 360) % 360;
    }
    
    public void setSaturation(double saturation) {
        this.saturation = Math.max(0.0, Math.min(1.0, saturation));
    }
    
    public void setValue(double value) {
        this.value = Math.max(0.0, Math.min(1.0, value));
    }
    
    // Frequency management
    public void incrementFrequency() {
        this.frequency++;
    }
    
    public void incrementFrequency(int amount) {
        this.frequency += amount;
    }
    
    // Calculate percentage
    public double getPercentage() {
        if (totalPixels == 0) {
            return 0.0;
        }
        return (frequency * 100.0) / totalPixels;
    }
    
    // Distance calculations
    public double rgbDistanceTo(ColorInfo other) {
        if (other == null) {
            return Double.MAX_VALUE;
        }
        double dr = this.red - other.red;
        double dg = this.green - other.green;
        double db = this.blue - other.blue;
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
    
    public double hsvDistanceTo(ColorInfo other) {
        if (other == null) {
            return Double.MAX_VALUE;
        }
        
        // Convert HSV to 3D coordinates for distance calculation
        double h1Rad = Math.toRadians(this.hue);
        double h2Rad = Math.toRadians(other.hue);
        
        double x1 = this.saturation * Math.cos(h1Rad);
        double y1 = this.saturation * Math.sin(h1Rad);
        double z1 = this.value;
        
        double x2 = other.saturation * Math.cos(h2Rad);
        double y2 = other.saturation * Math.sin(h2Rad);
        double z2 = other.value;
        
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    // Similarity check
    public boolean isSimilarTo(ColorInfo other, double threshold) {
        if (other == null) {
            return false;
        }
        return rgbDistanceTo(other) <= threshold;
    }
    
    // Get description
    public String getDescription() {
        if (name != null && !name.isEmpty()) {
            return String.format("%s (RGB: %d,%d,%d)", name, red, green, blue);
        }
        return String.format("RGB(%d,%d,%d)", red, green, blue);
    }
    
    // Convert to hex
    public String toHex() {
        return String.format("#%02X%02X%02X", red, green, blue);
    }
    
    // Comparable implementation (by frequency)
    @Override
    public int compareTo(ColorInfo other) {
        if (other == null) {
            return 1;
        }
        return Integer.compare(this.frequency, other.frequency);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorInfo colorInfo = (ColorInfo) o;
        return red == colorInfo.red &&
               green == colorInfo.green &&
               blue == colorInfo.blue;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue);
    }
}