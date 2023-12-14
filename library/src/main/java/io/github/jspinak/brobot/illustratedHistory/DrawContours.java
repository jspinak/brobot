package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.illustratedHistory.draw.DrawRect;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.boundingRect;

@Component
public class DrawContours {

    private DrawRect drawRect;

    public DrawContours(DrawRect drawRect) {
        this.drawRect = drawRect;
    }

    public void draw(Mat mat, MatVector contours) {
        for (int i=0; i<contours.size(); i++) {
            drawRect.drawRect(mat, boundingRect(contours.get(i)), new Scalar(220, 220, 220, 0));
        }
    }

    public void draw(Mat mat, List<Rect> contours) {
        for (Rect rect : contours) {
            drawRect.drawRectAroundMatch(mat, rect, new Scalar(220, 220, 220, 0));
        }
    }

    public void draw(SceneAnalysis sceneAnalysis) {
        Mat matchesOnClasses = sceneAnalysis.getIllustrations().getMatchesOnClasses();
        if (sceneAnalysis.getContours() == null) return;
        List<Rect> unmodifiedContours = sceneAnalysis.getContours().getScreenAdjustedContours();
        if (matchesOnClasses == null || unmodifiedContours.isEmpty()) return;
        draw(matchesOnClasses, unmodifiedContours);
    }
}
