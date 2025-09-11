package io.github.jspinak.brobot.tools.history.draw;

import static org.bytedeco.opencv.global.opencv_imgproc.line;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Location;

/**
 * Utility for drawing straight lines on images.
 *
 * <p>DrawLine provides flexible line drawing capabilities for creating connections, boundaries, and
 * visual indicators on screenshots and visualizations. Unlike {@link DrawArrow}, these lines have
 * no directional indicators, making them suitable for showing relationships, boundaries, or general
 * connections.
 *
 * <p><b>Visual Output Structure:</b>
 *
 * <ul>
 *   <li>Straight line between two points
 *   <li>Customizable thickness (1-10+ pixels typical)
 *   <li>Various line types (solid, anti-aliased)
 *   <li>No directional indicators or endpoints
 * </ul>
 *
 * <p><b>Configuration Parameters:</b>
 *
 * <ul>
 *   <li>Thickness: Customizable line width in pixels
 *   <li>Line type: 8 (anti-aliased), 4 (4-connected), 16 (anti-aliased)
 *   <li>Shift: Fractional bits for sub-pixel precision (typically 0)
 *   <li>Color: Fully customizable BGR color values
 * </ul>
 *
 * <p><b>Use Cases:</b>
 *
 * <ul>
 *   <li>Drawing borders around regions of interest
 *   <li>Connecting related UI elements visually
 *   <li>Creating grid lines or visual guides
 *   <li>Drawing histogram bars (as in {@link DrawHistogram})
 *   <li>Showing non-directional relationships between points
 *   <li>Creating custom shapes by combining multiple lines
 * </ul>
 *
 * <p><b>Relationships:</b>
 *
 * <ul>
 *   <li>Complements {@link DrawArrow} for non-directional connections
 *   <li>Used by {@link DrawHistogram} to create vertical bars
 *   <li>Often combined with {@link DrawPoint} to connect marked locations
 *   <li>Works with both {@link Location} and {@link Point} for coordinate flexibility
 * </ul>
 *
 * @see DrawArrow
 * @see DrawPoint
 * @see DrawHistogram
 * @see Location
 */
@Component
public class DrawLine {

    /**
     * Draws a line between two Location objects.
     *
     * <p>Creates a straight line from start to end using Location objects, which automatically
     * handle coordinate calculations. This method is convenient when working with Brobot's
     * Location-based coordinate system.
     *
     * <p><b>Line Type Options:</b>
     *
     * <ul>
     *   <li>4: 4-connected line (faster, less smooth)
     *   <li>8: 8-connected line (default, good quality)
     *   <li>16: Anti-aliased line (LINE_AA, smoothest)
     * </ul>
     *
     * @param scene the background Mat to draw on, typically a screenshot
     * @param start the starting location of the line
     * @param end the ending location of the line
     * @param color the line color in BGR format (e.g., new Scalar(0,255,0) for green)
     * @param thickness the line thickness in pixels (1 for thin, 5+ for bold)
     * @param lineType the algorithm for line drawing (4, 8, or 16/LINE_AA)
     * @param shift number of fractional bits in coordinates (0 for integer precision)
     */
    public void draw(
            Mat scene,
            Location start,
            Location end,
            Scalar color,
            int thickness,
            int lineType,
            int shift) {
        Point startPoint = new Point(start.getCalculatedX(), start.getCalculatedY());
        Point endPoint = new Point(end.getCalculatedX(), end.getCalculatedY());
        line(scene, startPoint, endPoint, color, thickness, lineType, shift);
    }

    /**
     * Draws a line between two Point objects.
     *
     * <p>Direct OpenCV Point-based line drawing for when you have exact pixel coordinates. This
     * overload bypasses Location calculations for maximum performance and precision.
     *
     * <p><b>Common Usage Patterns:</b>
     *
     * <ul>
     *   <li>Thickness 1-2: Thin lines for subtle connections
     *   <li>Thickness 3-5: Standard lines for clear visibility
     *   <li>Thickness 6+: Bold lines for emphasis
     *   <li>LineType 8: Standard quality for most uses
     *   <li>LineType 16: When smooth anti-aliasing is needed
     * </ul>
     *
     * @param scene the background Mat to draw on
     * @param start the exact starting point coordinates
     * @param end the exact ending point coordinates
     * @param color the line color in BGR format
     * @param thickness the line thickness in pixels
     * @param lineType the line drawing algorithm
     * @param shift fractional bits (typically 0)
     */
    public void draw(
            Mat scene,
            Point start,
            Point end,
            Scalar color,
            int thickness,
            int lineType,
            int shift) {
        line(scene, start, end, color, thickness, lineType, shift);
    }
}
