package io.github.jspinak.brobot.tools.history.draw;

import io.github.jspinak.brobot.model.element.Location;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_imgproc.arrowedLine;

/**
 * Utility for drawing directional arrows on images.
 * 
 * <p>DrawArrow creates visual indicators showing direction and movement between two points.
 * This is particularly useful for illustrating mouse movements, drag operations, or
 * showing relationships between UI elements in automation reports.</p>
 * 
 * <p><b>Visual Output Structure:</b></p>
 * <ul>
 *   <li>Solid line with an arrowhead at the end point</li>
 *   <li>Line thickness: 5 pixels</li>
 *   <li>Line type: 8 (8-connected line for anti-aliasing)</li>
 *   <li>Arrow tip length: 10% of the line length (0.1 ratio)</li>
 * </ul>
 * 
 * <p><b>Configuration Parameters:</b></p>
 * <ul>
 *   <li>Thickness: 5 pixels (fixed)</li>
 *   <li>Line type: 8 (anti-aliased)</li>
 *   <li>Shift: 0 (no fractional bits)</li>
 *   <li>Tip length ratio: 0.1 (10% of line length)</li>
 *   <li>Color: Customizable via method parameter</li>
 * </ul>
 * 
 * <p><b>Use Cases:</b></p>
 * <ul>
 *   <li>Visualizing drag operations from source to destination</li>
 *   <li>Showing mouse movement paths in automation sequences</li>
 *   <li>Indicating directional relationships between UI elements</li>
 *   <li>Creating visual documentation of user interactions</li>
 *   <li>Debugging complex mouse navigation sequences</li>
 * </ul>
 * 
 * <p><b>Relationships:</b></p>
 * <ul>
 *   <li>Often used with {@link DrawLine} for non-directional connections</li>
 *   <li>Complements {@link DrawPoint} for marking specific locations</li>
 *   <li>Used by motion visualization tools to show object movement</li>
 *   <li>Works with {@link Location} for precise coordinate handling</li>
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
     * <p>Creates an arrow with the following characteristics:</p>
     * <ul>
     *   <li>Starts at the exact start location coordinates</li>
     *   <li>Ends at the exact end location coordinates</li>
     *   <li>Arrowhead points toward the end location</li>
     *   <li>Uses anti-aliased rendering for smooth appearance</li>
     * </ul>
     * 
     * <p>The arrow is drawn directly on the provided scene matrix, modifying
     * the original image. For non-destructive drawing, create a copy of the
     * scene before calling this method.</p>
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
