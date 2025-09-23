package io.github.jspinak.brobot.tools.history.draw;

import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_imgproc.circle;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

/**
 * Utility for drawing circular point markers on images.
 *
 * <p>DrawPoint creates distinctive circular markers to highlight specific locations on screenshots
 * or visualizations. The markers use a multi-ring design for high visibility against various
 * backgrounds, making them ideal for marking click targets, match locations, or points of interest.
 *
 * <p><b>Visual Output Structure:</b>
 *
 * <ul>
 *   <li>Inner filled circle: 6-pixel radius in specified color
 *   <li>Middle ring: 8-pixel radius white outline
 *   <li>Outer ring: 10-pixel radius white outline
 *   <li>Total marker diameter: 20 pixels
 * </ul>
 *
 * <p><b>Configuration Parameters:</b>
 *
 * <ul>
 *   <li>Inner circle: 6px radius, filled, customizable color
 *   <li>Middle ring: 8px radius, white (255,255,255)
 *   <li>Outer ring: 10px radius, white (255,255,255)
 *   <li>Line type: LINE_8 (8-connected anti-aliased)
 *   <li>Fill type: FILLED for inner circle
 * </ul>
 *
 * <p><b>Use Cases:</b>
 *
 * <ul>
 *   <li>Marking match locations in pattern recognition results
 *   <li>Highlighting click targets in automation sequences
 *   <li>Indicating points of interest in visual debugging
 *   <li>Creating visual test reports with marked locations
 *   <li>Showing anchor points for region definitions
 * </ul>
 *
 * <p><b>Relationships:</b>
 *
 * <ul>
 *   <li>Works with {@link Match} objects to mark their target locations
 *   <li>Complements {@link DrawArrow} for showing directional relationships *
 *   <li>Pairs with {@link DrawLine} to connect marked points
 * </ul>
 *
 * @see DrawArrow
 * @see DrawLine
 * @see Match
 */
@Component
public class DrawPoint {

    /**
     * Draws a point marker at a match's target location.
     *
     * <p>Creates a three-ring circular marker at the match's target position, which is typically
     * the center of the matched region. The multi-ring design ensures visibility against various
     * backgrounds:
     *
     * <ul>
     *   <li>Colored center provides the primary visual indicator
     *   <li>White rings create contrast for visibility on dark backgrounds
     *   <li>Multiple rings prevent the marker from blending into the scene
     * </ul>
     *
     * @param scene the background Mat to draw on, typically a screenshot
     * @param match the match object whose target location will be marked
     * @param color the color for the inner filled circle in BGR format
     */
    public void fromMatch(Mat scene, Match match, Scalar color) {
        org.sikuli.script.Location loc = match.getTarget();
        Point target = new Point(loc.x, loc.y);
        circle(scene, target, 6, color, FILLED, LINE_8, 0); // fill
        circle(scene, target, 8, new Scalar(255));
        circle(scene, target, 10, new Scalar(255));
    }

    /**
     * Draws a point marker at a specific coordinate.
     *
     * <p>Creates the same three-ring circular marker as {@link #fromMatch}, but accepts direct
     * Point coordinates instead of a Match object. This provides flexibility for marking arbitrary
     * locations.
     *
     * <p>The marker design is identical to fromMatch:
     *
     * <ul>
     *   <li>6px filled circle in the specified color
     *   <li>8px and 10px white outline rings
     *   <li>Total 20px diameter for the complete marker
     * </ul>
     *
     * @param scene the background Mat to draw on
     * @param target the exact coordinates where the marker should be placed
     * @param color the color for the inner filled circle in BGR format
     */
    public void drawPoint(Mat scene, Point target, Scalar color) {
        circle(scene, target, 6, color, FILLED, LINE_8, 0); // fill
        circle(scene, target, 8, new Scalar(255));
        circle(scene, target, 10, new Scalar(255));
    }
}
