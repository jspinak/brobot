package io.github.jspinak.brobot.illustratedHistory.draw;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_imgproc.arrowedLine;

@Component
public class DrawArrow {

    /**
     * Draws an arrow from the start location to the end location.
     * @param scene is the background Mat, usually a screenshot.
     * @param start is the start location.
     * @param end is the end location.
     * @param color is the color in which to draw.
     */
    public void drawArrow(Mat scene, Location start, Location end, Scalar color) {
        Point startPoint = new Point(start.getX(), start.getY());
        Point endPoint = new Point(end.getX(), end.getY());
        arrowedLine(scene, startPoint, endPoint, color, 5, 8, 0, .1);
    }
}
