package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

/**
 * Adapted from https://stackoverflow.com/questions/2027613/how-to-draw-a-directed-arrow-line-in-java.
 */
@Component
public class DrawArrow {

    public void drawStandardArrow(Location from, Location to, Color insideColor, Graphics g) {
        draw(from, to, 5, Color.white, g);
        draw(from, to, 3, insideColor, g);
    }

    public void draw(Location from, Location to, int stroke, Color color, Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        g2D.setPaint(color);
        g2D.setStroke(new BasicStroke(stroke));

        // create an AffineTransform
        // and a triangle centered on (0,0) and pointing downward
        // somewhere outside Swing's paint loop
        AffineTransform tx = new AffineTransform();
        tx.setToIdentity();
        g2D.setTransform(tx);
        g2D.drawLine(from.getX(), from.getY(), to.getX(), to.getY());

        Line2D.Double line = new Line2D.Double(from.getX(), from.getY(), to.getX(), to.getY());

        Polygon arrowHead = new Polygon();
        int arrowSize = stroke * 2;
        arrowHead.addPoint( 0, arrowSize);
        arrowHead.addPoint( -arrowSize, -arrowSize);
        arrowHead.addPoint( arrowSize, -arrowSize);

        tx.setToIdentity();
        double angle = Math.atan2(line.y2-line.y1, line.x2-line.x1);
        tx.translate(line.x2, line.y2);
        tx.rotate((angle-Math.PI/2d));

        g2D.create();
        g2D.setTransform(tx);
        g2D.fill(arrowHead);
    }

}
