package io.github.jspinak.brobot.illustratedHistory.draw;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_imgproc.circle;

@Component
public class DrawPoint {

    /**
     * Draws a point at the match's target, which is usually the match center.
     * @param scene is the background Mat, usually a screenshot.
     * @param match this is the match to be drawn.
     * @param color is the color in which to draw.
     */
    public void fromMatch(Mat scene, Match match, Scalar color) {
        org.sikuli.script.Location loc = match.getTarget();
        Point target = new Point(loc.x, loc.y);
        circle(scene, target, 6, color, FILLED, LINE_8, 0); //fill
        circle(scene, target, 8, new Scalar(255));
        circle(scene, target, 10, new Scalar(255));
    }

    public void drawPoint(Mat scene, Point target, Scalar color) {
        circle(scene, target, 6, color, FILLED, LINE_8, 0); //fill
        circle(scene, target, 8, new Scalar(255));
        circle(scene, target, 10, new Scalar(255));
    }
}
