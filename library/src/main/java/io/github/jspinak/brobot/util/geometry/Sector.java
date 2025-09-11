package io.github.jspinak.brobot.util.geometry;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents an angular sector (arc) in a circular coordinate system.
 *
 * <p>Sector defines a portion of a circle bounded by two angles, automatically selecting the
 * shorter arc between them. This is useful for GUI automation scenarios involving circular
 * interfaces, radial menus, dial controls, or any situation where angular regions need to be
 * defined and tested for containment or intersection.
 *
 * <p>Key characteristics:
 *
 * <ul>
 *   <li><b>Automatic Optimization</b>: Always represents the shorter arc between two angles
 *   <li><b>Angle Normalization</b>: Handles angles outside 0-360 range
 *   <li><b>Clockwise Convention</b>: Sector spans from left to right angle clockwise
 *   <li><b>Full Circle Support</b>: Can represent complete 360° rotation
 *   <li><b>Original Values Preserved</b>: Stores input angles, not normalized values
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Radial menu navigation and selection
 *   <li>Circular slider or dial controls
 *   <li>Pie chart segment interaction
 *   <li>Clock-based interfaces
 *   <li>Compass or directional controls
 *   <li>Volume knobs and rotary controls
 * </ul>
 *
 * <p>Angle conventions:
 *
 * <ul>
 *   <li>0° = East (3 o'clock position)
 *   <li>90° = North (12 o'clock position)
 *   <li>180° = West (9 o'clock position)
 *   <li>270° = South (6 o'clock position)
 *   <li>Positive angles rotate counter-clockwise
 * </ul>
 *
 * <p>Algorithm details:
 *
 * <ol>
 *   <li>Both input angles are normalized to [0, 360) range
 *   <li>Clockwise span from angle1 to angle2 is calculated
 *   <li>Clockwise span from angle2 to angle1 is calculated
 *   <li>The shorter span determines the sector
 *   <li>Left angle is the start, right angle is the end (clockwise)
 * </ol>
 *
 * <p>Special cases:
 *
 * <ul>
 *   <li>Equal angles: Represents a full 360° circle
 *   <li>180° apart: Could go either direction, algorithm chooses consistently
 *   <li>Negative angles: Automatically normalized to positive equivalent
 *   <li>Large angles: Reduced to 0-360 range preserving direction
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>
 * // Define a sector from 45° to 135° (90° span)
 * Sector quadrant = new Sector(45, 135);
 *
 * // Define a sector from 315° to 45° (90° span crossing 0°)
 * Sector crossZero = new Sector(315, 45);
 *
 * // Full circle when angles are equal
 * Sector fullCircle = new Sector(90, 90);
 * </pre>
 *
 * <p>In the model-based approach, Sector enables precise interaction with circular GUI elements by
 * defining angular regions. This abstraction is essential for automating modern interfaces that use
 * radial layouts, circular gestures, or rotational controls, providing a mathematical foundation
 * for angular calculations.
 *
 * @since 1.0
 * @see Location
 * @see Region
 */
@Getter
@Setter
public class Sector {

    private double leftAngle;
    private double rightAngle;
    private double span;

    public Sector(double angle1, double angle2) {
        // Normalize angles to be in the range [0, 360) for calculation
        double normAngle1 = (angle1 % 360 + 360) % 360;
        double normAngle2 = (angle2 % 360 + 360) % 360;

        // Calculate the clockwise span from angle1 to angle2
        double span1_to_2 = (normAngle2 - normAngle1 + 360) % 360;

        // Calculate the clockwise span from angle2 to angle1
        double span2_to_1 = (normAngle1 - normAngle2 + 360) % 360;

        // The sector is defined by the shorter clockwise arc.
        // We store the original angle values, not the normalized ones.
        if (span1_to_2 <= span2_to_1) {
            this.leftAngle = angle1;
            this.rightAngle = angle2;
            this.span = span1_to_2 == 0 ? 360 : span1_to_2; // A span of 0 should be a full circle
        } else {
            this.leftAngle = angle2;
            this.rightAngle = angle1;
            this.span = span2_to_1 == 0 ? 360 : span2_to_1;
        }
    }
}
