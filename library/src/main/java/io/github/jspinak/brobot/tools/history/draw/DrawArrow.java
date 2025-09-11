package io.github.jspinak.brobot.tools.history.draw;

import static org.bytedeco.opencv.global.opencv_imgproc.arrowedLine;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Location;

/**
 * Utility for drawing directional arrows on images.
 *
 * <p>DrawArrow creates visual indicators showing direction and movement between two points. This is
 * particularly useful for illustrating mouse movements, drag operations, or showing relationships
 * between UI elements in automation reports.
 *
 * <p><b>Visual Output Structure:</b>
 *
 * <ul>
 *   <li>Solid line with an arrowhead at the end point
 *   <li>Line thickness: 5 pixels
 *   <li>Line type: 8 (8-connected line for anti-aliasing)
 *   <li>Arrow tip length: 10% of the line length (0.1 ratio)
 * </ul>
 *
 * <p><b>Configuration Parameters:</b>
 *
 * <ul>
 *   <li>Thickness: 5 pixels (fixed)
 *   <li>Line type: 8 (anti-aliased)
 *   <li>Shift: 0 (no fractional bits)
 *   <li>Tip length ratio: 0.1 (10% of line length)
 *   <li>Color: Customizable via method parameter
 * </ul>
 *
 * <p><b>Use Cases:</b>
 *
 * <ul>
 *   <li>Visualizing drag operations from source to destination
 *   <li>Showing mouse movement paths in automation sequences
 *   <li>Indicating directional relationships between UI elements
 *   <li>Creating visual documentation of user interactions
 *   <li>Debugging complex mouse navigation sequences
 * </ul>
 *
 * <p><b>Relationships:</b>
 *
 * <ul>
 *   <li>Often used with {@link DrawLine} for non-directional connections
 *   <li>Complements {@link DrawPoint} for marking specific locations
 *   <li>Used by motion visualization tools to show object movement
 *   <li>Works with {@link Location} for precise coordinate handling
 * </ul>
 *
 * @see DrawLine
 * @see DrawPoint
 * @see DrawMotion
 * @see Location
 */
@Component
public class DrawArrow {

    /**
     * Draws a directional arrow from start to end location.
     *
     * <p>Creates an arrow with the following characteristics:
     *
     * <ul>
     *   <li>Starts at the exact start location coordinates
     *   <li>Ends at the exact end location coordinates
     *   <li>Arrowhead points toward the end location
     *   <li>Uses anti-aliased rendering for smooth appearance
     * </ul>
     *
     * <p>The arrow is drawn directly on the provided scene matrix, modifying the original image.
     * For non-destructive drawing, create a copy of the scene before calling this method.
     *
     * @param scene the background Mat to draw on, typically a screenshot or visualization canvas
     * @param start the starting point of the arrow
     * @param end the ending point of the arrow (where the arrowhead points)
     * @param color the color of the arrow in BGR format (e.g., new Scalar(0,0,255) for red)
     */
    public void drawArrow(Mat scene, Location start, Location end, Scalar color) {
        Point startPoint = new Point(start.getCalculatedX(), start.getCalculatedY());
        Point endPoint = new Point(end.getCalculatedX(), end.getCalculatedY());
        arrowedLine(scene, startPoint, endPoint, color, 5, 8, 0, .1);
    }
}
