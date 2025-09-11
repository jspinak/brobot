package io.github.jspinak.brobot.tools.history.draw;

import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;

import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.history.visual.Visualization;

/**
 * Provides rectangle drawing utilities for visualizing matches and regions on OpenCV Mat images.
 *
 * <p>This class is a core component of the Brobot visualization system, offering various methods to
 * draw rectangles around or directly on matches, regions, and other rectangular areas. It handles
 * boundary checking to ensure rectangles stay within image bounds and provides both single and
 * batch drawing operations.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Draw rectangles around matches with 1-pixel padding
 *   <li>Draw rectangles directly on match boundaries
 *   <li>Batch operations for multiple regions
 *   <li>Automatic boundary clipping to prevent out-of-bounds drawing
 *   <li>Support for custom colors via OpenCV Scalar
 * </ul>
 *
 * <p>Drawing modes:
 *
 * <ul>
 *   <li><b>Around</b> - Adds 1-pixel padding around the target area
 *   <li><b>On</b> - Draws exactly on the target boundaries
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Highlighting found matches in automation results
 *   <li>Visualizing search regions during debugging
 *   <li>Creating annotated screenshots for documentation
 *   <li>Drawing bounding boxes for object detection results
 * </ul>
 *
 * <p>Thread safety: This class is stateless and thread-safe. Multiple threads can use the same
 * instance concurrently.
 *
 * @see DrawMatch
 * @see DrawLine
 * @see Visualization
 * @see org.bytedeco.opencv.global.opencv_imgproc#rectangle
 */
@Component
public class DrawRect {

    /**
     * Draws a rectangle around a match with 1-pixel padding on all sides.
     *
     * <p>This method adds visual emphasis by drawing the rectangle slightly outside the match
     * boundaries, making it easier to see the match edges. The rectangle is automatically clipped
     * to stay within the image bounds.
     *
     * <p>Boundary handling:
     *
     * <ul>
     *   <li>X coordinates are clamped to [0, image width]
     *   <li>Y coordinates are clamped to [0, image height]
     *   <li>1-pixel padding is added where possible
     * </ul>
     *
     * @param scene The OpenCV Mat image on which to draw. Must not be null.
     * @param match The match whose boundaries define the rectangle. Must not be null.
     * @param color The color of the rectangle as an OpenCV Scalar (e.g., new Scalar(0,255,0) for
     *     green).
     */
    public void drawRectAroundMatch(Mat scene, Match match, Scalar color) {
        int drawX = Math.max(0, match.x() - 1);
        int drawY = Math.max(0, match.y() - 1);
        int drawX2 = Math.min(scene.cols(), match.x() + match.w() + 1);
        int drawY2 = Math.min(scene.rows(), match.y() + match.h() + 1);
        Rect aroundMatch = new Rect(drawX, drawY, drawX2 - drawX, drawY2 - drawY);
        rectangle(scene, aroundMatch, color);
    }

    /**
     * Draws a rectangle directly on the match boundaries without padding.
     *
     * <p>This method draws the rectangle exactly on the match edges, useful when precise boundary
     * visualization is needed. The rectangle is automatically clipped to stay within the image
     * bounds.
     *
     * @param scene The OpenCV Mat image on which to draw. Must not be null.
     * @param match The match whose boundaries define the rectangle. Must not be null.
     * @param color The color of the rectangle as an OpenCV Scalar.
     */
    public void drawRectOnMatch(Mat scene, Match match, Scalar color) {
        int drawX = Math.max(0, match.x());
        int drawY = Math.max(0, match.y());
        int drawX2 = Math.min(scene.cols(), match.x() + match.w());
        int drawY2 = Math.min(scene.rows(), match.y() + match.h());
        Rect onMatch = new Rect(drawX, drawY, drawX2 - drawX, drawY2 - drawY);
        rectangle(scene, onMatch, color);
    }

    /**
     * Draws a rectangle directly on the specified OpenCV Rect boundaries.
     *
     * <p>This is a lower-level method that works directly with OpenCV Rect objects. The rectangle
     * is drawn exactly on the rect boundaries with automatic boundary clipping.
     *
     * @param mat The OpenCV Mat image on which to draw. Must not be null.
     * @param rect The OpenCV Rect defining the rectangle boundaries. Must not be null.
     * @param color The color of the rectangle as an OpenCV Scalar.
     */
    public void drawRect(Mat mat, Rect rect, Scalar color) {
        int drawX = Math.max(0, rect.x());
        int drawY = Math.max(0, rect.y());
        int drawX2 = Math.min(mat.cols(), rect.x() + rect.width());
        int drawY2 = Math.min(mat.rows(), rect.y() + rect.height());
        Rect onRect = new Rect(drawX, drawY, drawX2 - drawX, drawY2 - drawY);
        rectangle(mat, onRect, color);
    }

    /**
     * Draws a rectangle around an OpenCV Rect with 1-pixel padding.
     *
     * <p>Similar to {@link #drawRectAroundMatch(Mat, Match, Scalar)} but works directly with OpenCV
     * Rect objects. Adds 1-pixel padding on all sides where possible within image bounds.
     *
     * @param mat The OpenCV Mat image on which to draw. Must not be null.
     * @param rect The OpenCV Rect around which to draw. Must not be null.
     * @param color The color of the rectangle as an OpenCV Scalar.
     */
    public void drawRectAroundMatch(Mat mat, Rect rect, Scalar color) {
        int drawX = Math.max(0, rect.x() - 1);
        int drawY = Math.max(0, rect.y() - 1);
        int drawX2 = Math.min(mat.cols(), rect.x() + rect.width() + 1);
        int drawY2 = Math.min(mat.rows(), rect.y() + rect.height() + 1);
        Rect onRect = new Rect(drawX, drawY, drawX2 - drawX, drawY2 - drawY);
        rectangle(mat, onRect, color);
    }

    /**
     * Draws rectangles around multiple regions on both scene and class illustrations.
     *
     * <p>This batch operation method draws rectangles on two different visualization layers
     * provided by the Illustrations object. This is useful for showing the same regions on
     * different visual representations simultaneously.
     *
     * @param illScn The Illustrations object containing both scene and class visualizations.
     * @param searchRegions List of regions to draw rectangles around. Empty lists are handled
     *     safely.
     * @param color The color of the rectangles as an OpenCV Scalar.
     */
    public void drawRectAroundMatch(
            Visualization illScn, List<Region> searchRegions, Scalar color) {
        drawRectAroundRegions(illScn.getMatchesOnScene(), searchRegions, color);
        drawRectAroundRegions(illScn.getMatchesOnClasses(), searchRegions, color);
    }

    /**
     * Draws rectangles around multiple regions in a single batch operation.
     *
     * <p>This method efficiently draws rectangles around all provided regions. Each region is
     * converted to a Match object for drawing. Null Mat objects are handled safely by returning
     * early.
     *
     * @param mat The OpenCV Mat image on which to draw. Can be null (method returns early).
     * @param searchRegions List of regions to draw rectangles around. Each region is converted to a
     *     match.
     * @param color The color of the rectangles as an OpenCV Scalar.
     */
    public void drawRectAroundRegions(Mat mat, List<Region> searchRegions, Scalar color) {
        if (mat == null) return;
        for (Region searchRegion : searchRegions) {
            Match match = searchRegion.toMatch();
            drawRectAroundMatch(mat, match, color);
        }
    }

    /**
     * Draws a rectangle around a single region with 1-pixel padding.
     *
     * <p>Convenience method for drawing a rectangle around a single Region object. The region is
     * converted to a Match for drawing. Null Mat objects are handled safely.
     *
     * @param mat The OpenCV Mat image on which to draw. Can be null (method returns early).
     * @param region The region around which to draw the rectangle. Must not be null.
     * @param color The color of the rectangle as an OpenCV Scalar.
     */
    public void drawRectAroundRegion(Mat mat, Region region, Scalar color) {
        if (mat == null) return;
        drawRectAroundMatch(mat, region.toMatch(), color);
    }
}
