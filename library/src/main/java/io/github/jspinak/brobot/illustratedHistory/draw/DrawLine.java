package io.github.jspinak.brobot.illustratedHistory.draw;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_imgproc.line;

@Component
public class DrawLine {

    /**
     * Draws a line from the start location to the end location.
     * @param scene is the background Mat, usually a screenshot.
     * @param start is the start location.
     * @param end is the end location.
     * @param color is the color in which to draw.
     * @param thickness is the thickness of the line.
     * @param lineType is the type of the line.
     * @param shift is the number of fractional bits in the point coordinates.
     */
    public void draw(Mat scene, Location start, Location end, Scalar color, int thickness, int lineType, int shift) {
        Point startPoint = new Point(start.getX(), start.getY());
        Point endPoint = new Point(end.getX(), end.getY());
        line(scene, startPoint, endPoint, color, thickness, lineType, shift);
    }

    public void draw(Mat scene, Point start, Point end, Scalar color, int thickness, int lineType, int shift) {
        line(scene, start, end, color, thickness, lineType, shift);
    }
}
