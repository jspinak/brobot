package io.github.jspinak.brobot.illustratedHistory.draw;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.illustratedHistory.Illustrations;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;

@Component
public class DrawRect {

    /**
     * Draws a rectangle around the match.
     * @param scene is a Mat representing the screenshot.
     * @param match this is the match to be drawn.
     * @param color is the color in which to draw.
     */
    public void drawRectAroundMatch(Mat scene, Match match, Scalar color) {
        int drawX = Math.max(0, match.x()-1);
        int drawY = Math.max(0, match.y()-1);
        int drawX2 = Math.min(scene.cols(), match.x() + match.w() + 1);
        int drawY2 = Math.min(scene.rows(), match.y() + match.h() + 1);
        Rect aroundMatch = new Rect(drawX, drawY, drawX2-drawX, drawY2-drawY);
        rectangle(scene, aroundMatch, color);
    }

    public void drawRectOnMatch(Mat scene, Match match, Scalar color) {
        int drawX = Math.max(0, match.x());
        int drawY = Math.max(0, match.y());
        int drawX2 = Math.min(scene.cols(), match.x() + match.w());
        int drawY2 = Math.min(scene.rows(), match.y() + match.h());
        Rect onMatch = new Rect(drawX, drawY, drawX2-drawX, drawY2-drawY);
        rectangle(scene, onMatch, color);
    }

    public void drawRect(Mat mat, Rect rect, Scalar color) {
        int drawX = Math.max(0, rect.x());
        int drawY = Math.max(0, rect.y());
        int drawX2 = Math.min(mat.cols(), rect.x() + rect.width());
        int drawY2 = Math.min(mat.rows(), rect.y() + rect.height());
        Rect onRect = new Rect(drawX, drawY, drawX2-drawX, drawY2-drawY);
        rectangle(mat, onRect, color);
    }

    public void drawRectAroundMatch(Mat mat, Rect rect, Scalar color) {
        int drawX = Math.max(0, rect.x() - 1);
        int drawY = Math.max(0, rect.y() - 1);
        int drawX2 = Math.min(mat.cols(), rect.x() + rect.width() + 1);
        int drawY2 = Math.min(mat.rows(), rect.y() + rect.height() + 1);
        Rect onRect = new Rect(drawX, drawY, drawX2-drawX, drawY2-drawY);
        rectangle(mat, onRect, color);
    }

    public void drawRectAroundMatch(Illustrations illScn, List<Region> searchRegions, Scalar color) {
        drawRectAroundRegions(illScn.getMatchesOnScene(), searchRegions, color);
        drawRectAroundRegions(illScn.getMatchesOnClasses(), searchRegions, color);
    }

    public void drawRectAroundRegions(Mat mat, List<Region> searchRegions, Scalar color) {
        if (mat == null) return;
        for (Region searchRegion : searchRegions) {
            Match match = searchRegion.toMatch();
            drawRectAroundMatch(mat, match, color);
        }
    }

    public void drawRectAroundRegion(Mat mat, Region region, Scalar color) {
        if (mat == null) return;
        drawRectAroundMatch(mat, region.toMatch(), color);
    }
}
