package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.illustratedHistory.draw.DrawRect;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Component
public class Draw {

    private final DrawRect drawRect;

    public Draw(DrawRect drawRect) {
        this.drawRect = drawRect;
    }

    public void drawRect(Mat screen, Match match, Scalar color) {
        int drawX = Math.max(0, match.x()-1);
        int drawY = Math.max(0, match.y()-1);
        int drawX2 = Math.min(screen.cols(), match.x() + match.w() + 1);
        int drawY2 = Math.min(screen.rows(), match.y() + match.h() + 1);
        Rect aroundMatch = new Rect(drawX, drawY, drawX2-drawX, drawY2-drawY);
        rectangle(screen, aroundMatch, color);
    }

    public void drawPoint(Mat screen, Match match, Scalar color) {
        Location loc = match.getTarget();
        Point center = new Point(loc.getX(), loc.getY());
        circle(screen, center, 6, color, FILLED, LINE_8, 0); //fill
        circle(screen, center, 8, new Scalar(255));
        circle(screen, center, 10, new Scalar(255));
    }

    public void drawClick(Illustrations illustrations, Matches matches) {
        for (Match match : matches.getMatchList()) {
            Report.println("Drawing click on " + match.getTarget().getX() + ", " + match.getTarget().getY());
            drawPoint(illustrations.getMatchesOnScene(), match, new Scalar(255, 150, 255, 0));
        }
    }

    public void drawArrow(Mat screen, Location start, Location end, Scalar color) {
        Point startPoint = new Point(start.getX(), start.getY());
        Point endPoint = new Point(end.getX(), end.getY());
        arrowedLine(screen, startPoint, endPoint, color, 5, 8, 0, .04);
    }

    public void drawDrag(Illustrations illustrations, Matches matches) {
        if (matches.size() < 2) return;
        List<Match> matchList = new ArrayList<>(matches.getMatchList());
        for (int i = 0; i < matchList.size() - 1; i++) {
            Match match = matchList.get(i);
            Match nextMatch = matchList.get(i + 1);
            drawArrow(illustrations.getMatchesOnScene(), new Location(match), new Location(nextMatch), new Scalar(255, 150, 255, 0));
        }
    }

    public void drawDefinedRegion(Illustrations illustrations, Matches matches) {
        System.out.println("illustrations.getScene() rows = " + illustrations.getScene().rows());
        drawRect.drawRectAroundRegion(illustrations.getScene(), matches.getDefinedRegion(), new Scalar(10, 10, 10, 255)); // draw defined region
    }

    /**
     * The first location in the parameter location list gives the first position.
     * If there is only one Location, it will show up as a dot. If there are more than
     * one, a line(s) will be drawn. If the previous action was a Move action, it will be
     * included at the beginning of the locations list to show the movement from one point to another.
     *
     * @param screen is a Mat representing the screenshot.
     * @param locations are all Locations that describe a move.
     * @param color is the color in which to draw.
     */
    public void move(Mat screen, List<Location> locations, Scalar color) {
        if (locations.size() == 1) drawPoint(screen, locations.get(0).toMatch(), color);
        else for (int i=0; i<locations.size()-1; i++)
            drawArrow(screen, locations.get(i), locations.get(i+1), color);
    }

    public void drawMove(Illustrations illustrations, Matches matches, Location... startingPositions) {
        List<Location> locations = new ArrayList<>(List.of(startingPositions));
        locations.addAll(matches.getMatchLocations());
        move(illustrations.getMatchesOnScene(), locations, new Scalar(255, 150, 255, 0));
    }

}
