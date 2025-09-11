package io.github.jspinak.brobot.model.analysis.color;

import static io.github.jspinak.brobot.model.analysis.color.ColorSchema.ColorValue.*;

import lombok.Getter;
import lombok.Setter;

/**
 * BGR color schema implementation for OpenCV-compatible color matching.
 *
 * <p>Extends {@link ColorSchema} to provide BGR (Blue-Green-Red) color space representation. BGR is
 * the default color format used by OpenCV, making this schema essential for direct color matching
 * operations without conversion overhead.
 *
 * <p>BGR components:
 *
 * <ul>
 *   <li><b>Blue (B)</b>: Blue channel intensity (0-255)
 *   <li><b>Green (G)</b>: Green channel intensity (0-255)
 *   <li><b>Red (R)</b>: Red channel intensity (0-255)
 * </ul>
 *
 * <p>Characteristics:
 *
 * <ul>
 *   <li>Direct representation of pixel data from OpenCV Mat objects
 *   <li>No color space conversion required for matching
 *   <li>Suitable for exact color matching scenarios
 *   <li>More sensitive to lighting variations than HSV
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Matching UI elements with consistent lighting
 *   <li>Detecting specific RGB color values
 *   <li>Performance-critical applications avoiding color conversion
 *   <li>Working with color data from screenshots or captures
 * </ul>
 *
 * <p>Note: BGR order (not RGB) is used to maintain compatibility with OpenCV's internal
 * representation. When specifying color values manually, remember to provide them in Blue-Green-Red
 * order.
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
