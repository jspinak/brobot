package io.github.jspinak.brobot.illustratedHistory.draw;

import io.github.jspinak.brobot.imageUtils.MatOps;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DrawHistogram {

    private DrawLine drawLine;

    public DrawHistogram(DrawLine drawLine) {
        this.drawLine = drawLine;
    }

    public Mat draw(int histWidth, int histHeight, Mat histogram) {
        Mat histImage = new Mat(histHeight, histWidth, 16, new Scalar(0, 0, 0, 0));
        // the histogram values are only on the first column of the histogram matrix
        double[] histValues = MatOps.getDoubleColumn(0, histogram);
        int axisSize = histValues.length;
        double adjustmentX = (double) histImage.rows() / axisSize;
        int maxValue = (int) Arrays.stream(histValues).max().getAsDouble();
        double adjustmentY = (double) histImage.rows() / maxValue;
        for (int i=0; i<histValues.length; i++) {
            int x = (int) (i * adjustmentX);
            double binVal = histValues[i];
            int y = Math.max(0, (int) (histImage.rows() - binVal * adjustmentY));
            Point lineBottom = new Point(x, histHeight);
            Point lineTop = new Point(x, y);
            Scalar color = new Scalar(255, 255, 255, 0);
            drawLine.draw(histImage, lineBottom, lineTop, color, 2, 50, 0);
        }
        return histImage;
    }
}
