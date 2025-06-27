package io.github.jspinak.brobot.model.analysis.color;

import lombok.Getter;
import lombok.Setter;

import static io.github.jspinak.brobot.model.analysis.color.ColorSchema.ColorValue.*;

/**
 * BGR color schema implementation for OpenCV-compatible color matching.
 * 
 * <p>Extends {@link ColorSchema} to provide BGR (Blue-Green-Red) color space
 * representation. BGR is the default color format used by OpenCV, making this
 * schema essential for direct color matching operations without conversion overhead.</p>
 * 
 * <p>BGR components:
 * <ul>
 *   <li><b>Blue (B)</b>: Blue channel intensity (0-255)</li>
 *   <li><b>Green (G)</b>: Green channel intensity (0-255)</li>
 *   <li><b>Red (R)</b>: Red channel intensity (0-255)</li>
 * </ul>
 * </p>
 * 
 * <p>Characteristics:
 * <ul>
 *   <li>Direct representation of pixel data from OpenCV Mat objects</li>
 *   <li>No color space conversion required for matching</li>
 *   <li>Suitable for exact color matching scenarios</li>
 *   <li>More sensitive to lighting variations than HSV</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Matching UI elements with consistent lighting</li>
 *   <li>Detecting specific RGB color values</li>
 *   <li>Performance-critical applications avoiding color conversion</li>
 *   <li>Working with color data from screenshots or captures</li>
 * </ul>
 * </p>
 * 
 * <p>Note: BGR order (not RGB) is used to maintain compatibility with OpenCV's
 * internal representation. When specifying color values manually, remember to
 * provide them in Blue-Green-Red order.</p>
 * 
 * @see ColorSchema
 * @see ColorSchemaHSV
 * @see ColorInfo
 */
@Getter
@Setter
public class ColorSchemaBGR extends ColorSchema {

    public ColorSchemaBGR() {
        super(BLUE, GREEN, RED);
    }
}
