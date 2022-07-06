package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import lombok.Getter;
import lombok.Setter;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.opencv.imgproc.Imgproc.*;

@Component
public class Draw {

    public void drawRect(Mat screen, Match match, Scalar color) {
        int drawX = Math.max(0, match.x-1);
        int drawY = Math.max(0, match.y-1);
        int drawX2 = Math.min(screen.cols(), match.x + match.w + 1);
        int drawY2 = Math.min(screen.rows(), match.y + match.h + 1);
        Rect aroundMatch = new Rect(drawX, drawY, drawX2-drawX, drawY2-drawY);
        rectangle(screen, aroundMatch, color, 1);
    }

    public void drawPoint(Mat screen, Match match, Scalar color) {
        org.sikuli.script.Location loc = match.getTarget();
        org.opencv.core.Point center = new Point(loc.x, loc.y);
        circle(screen, center, 6, color, -1); //fill
        circle(screen, center, 8, new Scalar(255,255,255));
        circle(screen, center, 10, new Scalar(255,255,255));
    }

    public void drawArrow(Mat screen, Location start, Location end, Scalar color) {
        Point startPoint = new Point(start.getX(), start.getY());
        Point endPoint = new Point(end.getX(), end.getY());
        arrowedLine(screen, startPoint, endPoint, color, 5);
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

}
