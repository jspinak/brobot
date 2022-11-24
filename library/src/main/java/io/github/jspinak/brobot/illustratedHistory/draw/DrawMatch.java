package io.github.jspinak.brobot.illustratedHistory.draw;

import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.illustratedHistory.Illustrations;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

@Component
public class DrawMatch {

    private final DrawRect drawRect;

    public DrawMatch(DrawRect drawRect) {
        this.drawRect = drawRect;
    }

    public void drawMatches(Mat scene, Matches matches) {
        matches.getMatches().forEach(m -> drawRect.drawRectAroundMatch(scene, m, new Scalar(255, 150, 255, 0)));
    }

    public void drawMatches(Illustrations illScn, Matches matches) {
        Mat matchesOnScene = illScn.getMatchesOnScene();
        if (matchesOnScene != null) drawMatches(illScn.getMatchesOnScene(), matches);
        Mat matchesOnClasses = illScn.getMatchesOnClasses();
        if (matchesOnClasses != null) drawMatches(illScn.getMatchesOnClasses(), matches);
    }
}
