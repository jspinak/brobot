package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import lombok.Getter;
import lombok.Setter;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;

@Component
@Setter
@Getter
public class Draw {

    private DrawArrow drawArrow;
    private DrawCircle drawCircle;
    private DrawRectangle drawRectangle;

    public Draw(DrawArrow drawArrow, DrawCircle drawCircle, DrawRectangle drawRectangle) {
        this.drawArrow = drawArrow;
        this.drawCircle = drawCircle;
        this.drawRectangle = drawRectangle;
    }

    public void match(Match m, Graphics g, Color color) {
        //drawRectangle.draw(m.x, m.y, m.w, m.h, 4, Color.lightGray, false, g);
        drawRectangle.draw(m.x, m.y, m.w, m.h, 2, color, false, g);
        drawRectangle.draw(m.x, m.y, m.w-1, m.h-1, 1, Color.white, false, g);
        g.dispose();
    }

    public void click(Location l, Graphics g) {
        drawCircle.drawPoint(l, Color.green, g);
        g.dispose();
    }

    public void drag(Location from, Location to, Graphics g) {
        drawArrow.drawStandardArrow(from, to, Color.green, g);
        g.dispose();
    }

    /**
     * The first location in the parameter location list gives the ending position.
     * If there is only one Location, it will show up as a dot. If there are more than
     * one, a line(s) will be drawn. If the previous action was a Move action, it will be
     * included at the end of the locations list to show the movement from one point to another.
     *
     * @param locations All Locations that describe a move. Most recent first.
     */
    public void move(List<Location> locations, Graphics g) {
        if (locations.size() == 1) drawCircle.drawPoint(locations.get(0), Color.blue, g);
        else for (int i=0; i<locations.size()-1; i++)
            drawArrow.drawStandardArrow(locations.get(i+1), locations.get(i), Color.blue, g);
        g.dispose();
    }


}
