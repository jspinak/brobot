package io.github.jspinak.brobot.illustratedHistory;

import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class DrawRectangle {

    public void draw(int x, int y, int w, int h, int stroke, Color color, boolean fill, Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        g2D.setPaint(color);
        g2D.setStroke(new BasicStroke(stroke));
        if (fill) g2D.fillRect(x, y, w, h);
        else g2D.drawRect(x, y, w, h);
    }
}
