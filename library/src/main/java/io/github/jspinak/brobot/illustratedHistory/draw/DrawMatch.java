package io.github.jspinak.brobot.illustratedHistory.draw;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.illustratedHistory.Illustrations;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DrawMatch {

    private final DrawRect drawRect;

    public DrawMatch(DrawRect drawRect) {
        this.drawRect = drawRect;
    }

    public void drawMatches(Mat scene, List<Match> matchList) {
        matchList.forEach(m -> drawRect.drawRectAroundMatch(scene, m, new Scalar(255, 150, 255, 0)));
    }

    public void drawMatches(Mat scene, List<Match> matchList, Scalar color) {
        matchList.forEach(m -> drawRect.drawRectAroundMatch(scene, m, color));
    }

    public void drawMatches(Illustrations illScn, Matches matches) {
        List<Match> matchList = new ArrayList<>();
        matches.getMatchList().forEach(mO -> {
            if (mO.getScene().getName().equals(illScn.getSceneName())) {
                matchList.add(mO);
            }
        });
        if (illScn.getMatchesOnScene() != null) drawMatches(illScn.getMatchesOnScene(), matchList);
        if (illScn.getMatchesOnClasses() != null) drawMatches(illScn.getMatchesOnClasses(), matchList);
    }
}
