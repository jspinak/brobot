package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class DrawCircle {

    public void drawPoint(Location l, Color insideColor, Graphics g) {
        draw(l.getX(), l.getY(), 8, Color.white, true, g);
        draw(l.getX(), l.getY(), 6, insideColor, true, g);
        draw(l.getX(), l.getY(), 10, Color.lightGray, false, g);
        draw(l.getX(), l.getY(), 14, Color.lightGray, false, g);
    }

    public void draw(int x, int y, int w, Color color, boolean fill, Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        g2D.setPaint(color);
        g2D.setStroke(new BasicStroke(1));
        if (fill) g2D.fillOval(x - w/2, y - w/2, w, w);
        else g2D.drawOval(x - w/2, y - w/2, w, w);
    }
}
