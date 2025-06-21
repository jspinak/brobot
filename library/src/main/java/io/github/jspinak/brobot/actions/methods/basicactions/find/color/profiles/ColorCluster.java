package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import io.github.jspinak.brobot.report.Report;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages color profiles across multiple color spaces for pattern matching in Brobot.
 * 
 * <p>ColorCluster serves as a container for color analysis data across different color 
 * schemas (BGR and HSV), enabling sophisticated color-based pattern matching. Unlike 
 * k-means clustering approaches that identify dominant colors, ColorCluster uses 
 * statistical analysis of color ranges to create robust color profiles that can handle 
 * variations in lighting and display conditions.</p>
 * 
 * <p>Architecture:
 * <ul>
 *   <li><b>ColorCluster</b>: Top-level container for all color schemas</li>
 *   <li><b>ColorSchema</b>: Color space-specific data (BGR or HSV)</li>
 *   <li><b>ColorInfo</b>: Statistical data for each color channel (min, max, mean, stddev)</li>
 * </ul>
 * </p>
 * 
 * <p>Color analysis approach:
 * <ul>
 *   <li>Define color ranges through minimum and maximum values</li>
 *   <li>Calculate statistical properties (mean, standard deviation) within ranges</li>
 *   <li>Support multiple color spaces for robust matching</li>
 *   <li>Enable tolerance-based matching using statistical distributions</li>
 * </ul>
 * </p>
 * 
 * <p>Supported color schemas:
 * <ul>
 *   <li><b>BGR</b>: Blue-Green-Red color space used by OpenCV</li>
 *   <li><b>HSV</b>: Hue-Saturation-Value for lighting-invariant matching</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Identifying UI elements by their color signatures</li>
 *   <li>Detecting state changes through color variations</li>
 *   <li>Finding regions with specific color characteristics</li>
 *   <li>Creating lighting-tolerant pattern matching</li>
 * </ul>
 * </p>
 * 
 * <p>Mat generation:
 * <ul>
 *   <li>Can generate OpenCV Mat objects representing color statistics</li>
 *   <li>Supports different statistical representations (mean, min, max)</li>
 *   <li>Enables visual debugging of color profiles</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, ColorCluster enables robust visual element detection 
 * that goes beyond simple pixel matching. By maintaining statistical color profiles across 
 * multiple color spaces, it provides tolerance to the natural variations found in real-world 
 * GUI applications while maintaining high accuracy in element identification.</p>
 * 
 * @since 1.0
 * @see ColorSchema
 * @see ColorInfo
 * @see ColorStatProfile
 * @see KmeansProfile
 */
@Getter
@Setter
public class ColorCluster {

    public enum ColorSchemaName {
        HSV, BGR
    }

    private Map<ColorSchemaName, ColorSchema> colorSchemas = new HashMap<>();

    public ColorSchema getSchema(ColorSchemaName colorSchemaName) {
        return colorSchemas.get(colorSchemaName);
    }

    public void setSchema(ColorSchemaName colorSchemaName, ColorSchema colorSchema) {
        colorSchemas.put(colorSchemaName, colorSchema);
    }

    public Optional<ColorInfo> getInfo(ColorSchema.ColorValue colorValue) {
        for (ColorSchema colorSchema : colorSchemas.values()) {
            if (colorSchema.contains(colorValue))
                return Optional.of(colorSchema.getColorInfos().get(colorValue));
        }
        return Optional.empty();
    }

    public Mat getMat(ColorSchemaName colorSchemaName, ColorInfo.ColorStat colorStat, Size size) {
        if (!colorSchemas.containsKey(colorSchemaName)) {
            Report.println("ColorProfile does not have a Schema "+colorSchemaName);
            return new Mat();
        }
        return colorSchemas.get(colorSchemaName).getMat(colorStat, size);
    }

    public void put(ColorSchemaName colorSchemaName, ColorSchema colorSchema) {
        colorSchemas.put(colorSchemaName, colorSchema);
    }

    public void print() {
        for (ColorSchema colorSchema : colorSchemas.values()) {
            colorSchema.print();
        }
    }

}
