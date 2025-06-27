package io.github.jspinak.brobot.model.analysis.color;

import static io.github.jspinak.brobot.model.analysis.color.ColorSchema.ColorValue.*;

/**
 * HSV color schema implementation for color-based pattern matching.
 * 
 * <p>Extends {@link ColorSchema} to provide HSV (Hue-Saturation-Value) color space
 * representation. HSV is particularly useful for color-based matching because it
 * separates color information (hue) from lighting conditions (value/brightness),
 * making it more robust to variations in illumination.</p>
 * 
 * <p>HSV components:
 * <ul>
 *   <li><b>Hue (H)</b>: Color type (0-179 in OpenCV, representing 0-360 degrees)</li>
 *   <li><b>Saturation (S)</b>: Color purity (0-255, where 0 is grayscale)</li>
 *   <li><b>Value (V)</b>: Brightness (0-255, where 0 is black)</li>
 * </ul>
 * </p>
 * 
 * <p>Advantages over BGR:
 * <ul>
 *   <li>More intuitive color selection and range definition</li>
 *   <li>Better tolerance to lighting variations</li>
 *   <li>Easier to define color ranges for specific hues</li>
 *   <li>Natural separation of chromatic and achromatic information</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Detecting UI elements by their color regardless of brightness</li>
 *   <li>Finding colored text or icons under varying lighting</li>
 *   <li>Identifying state indicators (red/green status lights)</li>
 *   <li>Color-based region segmentation</li>
 * </ul>
 * </p>
 * 
 * <p>Note: When working with hue values, remember that hue is circular (0 and 179 
 * are adjacent). Special handling may be required for red colors that wrap around 
 * the hue circle.</p>
 * 
 * @see ColorSchema
 * @see ColorSchemaBGR
 * @see ColorInfo
 */
public class ColorSchemaHSV extends ColorSchema {

    public ColorSchemaHSV() {
        super(HUE, SATURATION, VALUE);
    }

}
