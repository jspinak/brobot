package io.github.jspinak.brobot.tools.history.draw;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.boundingRect;

/**
 * Utility for visualizing object contours and boundaries.
 * 
 * <p>DrawContours specializes in drawing bounding rectangles around detected contours,
 * which are typically the outlines of objects found through image processing operations.
 * Rather than drawing the actual contour paths, this class draws rectangular bounding
 * boxes that encompass each contour, providing a cleaner visualization of object boundaries.</p>
 * 
 * <p><b>Visual Output Structure:</b></p>
 * <ul>
 *   <li>Rectangular bounding boxes around each contour</li>
 *   <li>Light gray color (220,220,220) for visibility without distraction</li>
 *   <li>Standard rectangle outlines (not filled)</li>
 *   <li>Each contour gets its own bounding rectangle</li>
 * </ul>
 * 
 * <p><b>Configuration Parameters:</b></p>
 * <ul>
 *   <li>Color: Fixed light gray (220,220,220,0)</li>
 *   <li>Style: Outline rectangles via {@link DrawRect}</li>
 *   <li>Thickness: Inherited from DrawRect implementation</li>
 *   <li>Bounding method: Minimum enclosing rectangle</li>
 * </ul>
 * 
 * <p><b>Use Cases:</b></p>
 * <ul>
 *   <li>Visualizing object detection results from computer vision algorithms</li>
 *   <li>Debugging contour finding operations in image analysis</li>
 *   <li>Showing boundaries of detected UI elements or regions</li>
 *   <li>Creating visual reports of scene analysis results</li>
 *   <li>Illustrating segmentation results with clear boundaries</li>
 * </ul>
 * 
 * <p><b>Relationships:</b></p>
 * <ul>
 *   <li>Uses {@link DrawRect} for all rectangle drawing operations</li>
 *   <li>Integrates with {@link SceneAnalysis} to visualize analysis results</li>
 *   <li>Works with OpenCV's {@link MatVector} for raw contour data</li>
 *   <li>Handles pre-computed {@link Rect} lists for efficiency</li>
 * </ul>
 * 
 * @see DrawRect
 * @see SceneAnalysis
 * @see MatVector
 */
@Component
public class DrawContours {

    private DrawRect drawRect;

    public DrawContours(DrawRect drawRect) {
        this.drawRect = drawRect;
    }

    /**
     * Draws bounding rectangles for OpenCV contours.
     * 
     * <p>Processes raw OpenCV contour data and draws a bounding rectangle
     * around each contour. The bounding rectangles are computed using OpenCV's
     * boundingRect function, which finds the minimum upright rectangle that
     * contains all contour points.</p>
     * 
     * <p>This method is typically used when working directly with OpenCV
     * contour detection results, such as from findContours operations.</p>
     * 
     * @param mat the target matrix to draw on, typically a screenshot or analysis image
     * @param contours OpenCV MatVector containing detected contours
     */
    public void draw(Mat mat, MatVector contours) {
        for (int i=0; i<contours.size(); i++) {
            drawRect.drawRect(mat, boundingRect(contours.get(i)), new Scalar(220, 220, 220, 0));
        }
    }

    /**
     * Draws pre-computed bounding rectangles.
     * 
     * <p>Draws rectangles from a list of pre-computed bounding boxes.
     * This is more efficient than the MatVector version when bounding
     * rectangles have already been calculated, avoiding redundant
     * boundingRect computations.</p>
     * 
     * <p>Each rectangle is drawn in light gray to provide clear but
     * non-intrusive visualization of object boundaries.</p>
     * 
     * @param mat the target matrix to draw on
     * @param contours list of pre-computed bounding rectangles to draw
     */
    public void draw(Mat mat, List<Rect> contours) {
        for (Rect rect : contours) {
            drawRect.drawRectAroundMatch(mat, rect, new Scalar(220, 220, 220, 0));
        }
    }

    /**
     * Draws contours from a scene analysis result.
     * 
     * <p>Extracts contour information from a {@link SceneAnalysis} object
     * and draws them on the analysis illustration. This method handles:</p>
     * <ul>
     *   <li>Null checks for required components</li>
     *   <li>Screen coordinate adjustment for contours</li>
     *   <li>Integration with the illustration system</li>
     * </ul>
     * 
     * <p>The contours are drawn on the "matchesOnClasses" illustration layer,
     * allowing them to be visualized alongside classification results.</p>
     * 
     * @param sceneAnalysis the analysis results containing contour data and illustrations
     */
    public void draw(SceneAnalysis sceneAnalysis) {
        Mat matchesOnClasses = sceneAnalysis.getIllustrations().getMatchesOnClasses();
        if (sceneAnalysis.getContours() == null) return;
        List<Rect> unmodifiedContours = sceneAnalysis.getContours().getScreenAdjustedContours();
        if (matchesOnClasses == null || unmodifiedContours.isEmpty()) return;
        draw(matchesOnClasses, unmodifiedContours);
    }
}
