package io.github.jspinak.brobot.datatypes.primitives.location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a relative position within a rectangular area using percentage coordinates.
 * 
 * <p>Position provides a resolution-independent way to specify locations within regions, 
 * matches, or other rectangular areas. By using percentages (0.0 to 1.0) instead of absolute 
 * pixel coordinates, Position enables automation scripts that adapt automatically to different 
 * screen sizes, resolutions, and element dimensions.</p>
 * 
 * <p>Key advantages over absolute positioning:
 * <ul>
 *   <li><b>Resolution Independence</b>: Works across different screen sizes without modification</li>
 *   <li><b>Semantic Clarity</b>: Can use named positions like TOPLEFT, CENTER, BOTTOMRIGHT</li>
 *   <li><b>Dynamic Adaptation</b>: Automatically adjusts to changing element sizes</li>
 *   <li><b>Intuitive Specification</b>: Easy to target general areas without pixel precision</li>
 * </ul>
 * </p>
 * 
 * <p>Coordinate system:
 * <ul>
 *   <li>(0.0, 0.0) = top-left corner</li>
 *   <li>(0.5, 0.5) = center (default)</li>
 *   <li>(1.0, 1.0) = bottom-right corner</li>
 *   <li>Values can exceed 0-1 range for positions outside the area</li>
 * </ul>
 * </p>
 * 
 * <p>Construction options:
 * <ul>
 *   <li>Direct percentages: Position(0.25, 0.75)</li>
 *   <li>Integer percentages: Position(25, 75) converts to (0.25, 0.75)</li>
 *   <li>Named positions: Position(Positions.Name.BOTTOMLEFT)</li>
 *   <li>Named with offset: Position(TOPLEFT, 0.1, 0.1)</li>
 *   <li>Copy constructor: Position(existingPosition)</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Clicking in the center of found images regardless of size</li>
 *   <li>Targeting corners or edges of UI elements</li>
 *   <li>Defining relative search areas within larger regions</li>
 *   <li>Creating resolution-independent automation scripts</li>
 * </ul>
 * </p>
 * 
 * <p>Mathematical operations:
 * <ul>
 *   <li>Addition: Offset positions by percentage amounts</li>
 *   <li>Multiplication: Scale positions for zoom effects</li>
 *   <li>Supports method chaining for complex transformations</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Position enables abstract spatial reasoning that's independent 
 * of concrete pixel coordinates. This abstraction is crucial for creating maintainable automation 
 * that works across different environments and adapts to UI changes without script modifications.</p>
 * 
 * @since 1.0
 * @see Location
 * @see Positions
 * @see Region
 * @see Pattern
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Position {

    private double percentW;
    private double percentH;

    public Position() {
        this.percentW = 0.5;
        this.percentH = 0.5;
    }

    public Position(double w, double h) {
        this.percentW = w;
        this.percentH = h;
    }

    public Position(int percentW, int percentH) {
        this.percentW = (double) percentW / 100;
        this.percentH = (double) percentH / 100;
    }

    public Position(Positions.Name positionName) {
        this.percentW = Positions.getCoordinates(positionName).getKey();
        this.percentH = Positions.getCoordinates(positionName).getValue();
    }

    public Position(Positions.Name positionName, double addPercentW, double addPercentH) {
        this.percentW = Positions.getCoordinates(positionName).getKey() + addPercentW;
        this.percentH = Positions.getCoordinates(positionName).getValue() + addPercentH;
    }

    public Position(Position clone) {
        this.percentW = clone.percentW;
        this.percentH = clone.percentH;
    }

    public void addPercentW(double addW) {
        percentW += addW;
    }

    public void addPercentH(double addH) {
        percentH += addH;
    }

    public void multiplyPercentW(double mult) {
        percentW = percentW * mult;
    }

    public void multiplyPercentH(double mult) {
        percentH = percentH * mult;
    }

    @Override
    public String toString() {
        return String.format("P[%.1f %.1f]", percentW, percentH);
    }

}
