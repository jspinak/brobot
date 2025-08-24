package io.github.jspinak.brobot.tools.history.draw;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.util.image.core.MatrixUtilities;

import java.util.Arrays;

/**
 * Visualizes histogram data as vertical bar charts.
 * 
 * <p>DrawHistogram creates visual representations of histogram data, commonly used
 * for displaying color distribution, frequency analysis, or other statistical data.
 * The histogram is rendered as a series of vertical lines from the bottom of the
 * image to the data point height.</p>
 * 
 * <p><b>Visual Output Structure:</b></p>
 * <ul>
 *   <li>Black transparent background</li>
 *   <li>White vertical lines (255,255,255) representing data values</li>
 *   <li>Line thickness: 2 pixels</li>
 *   <li>Lines extend from bottom to normalized data height</li>
 *   <li>X-axis represents histogram bins</li>
 *   <li>Y-axis represents frequency/magnitude</li>
 * </ul>
 * 
 * <p><b>Configuration Parameters:</b></p>
 * <ul>
 *   <li>Customizable width and height via method parameters</li>
 *   <li>Line color: White (255,255,255,0)</li>
 *   <li>Line thickness: 2 pixels</li>
 *   <li>Line type: 50 (likely anti-aliased)</li>
 *   <li>Automatic scaling for both axes</li>
 * </ul>
 * 
 * <p><b>Use Cases:</b></p>
 * <ul>
 *   <li>Visualizing color channel distributions in images</li>
 *   <li>Displaying frequency analysis results</li>
 *   <li>Creating visual debugging tools for histogram-based algorithms</li>
 *   <li>Generating reports with statistical visualizations</li>
 * </ul>
 * 
 * <p><b>Relationships:</b></p>
 * <ul>
 *   <li>Uses {@link DrawLine} for rendering individual histogram bars</li>
 *   <li>Often used with color analysis tools to show channel distributions</li>
 *   <li>Complements {@link DrawColorProfile} for comprehensive color visualization</li>
 * </ul>
 * 
 * @see DrawLine
 * @see DrawColorProfile
 * @see MatrixUtilities
 */
@Component
public class DrawHistogram {

    private DrawLine drawLine;

    public DrawHistogram(DrawLine drawLine) {
        this.drawLine = drawLine;
    }

    /**
     * Draws a histogram visualization from raw histogram data.
     * 
     * <p>Converts histogram values into a visual bar chart representation.
     * The method performs the following steps:</p>
     * <ol>
     *   <li>Creates a black transparent canvas of specified dimensions</li>
     *   <li>Extracts histogram values from the first column of input matrix</li>
     *   <li>Calculates scaling factors for both X and Y axes</li>
     *   <li>Draws vertical lines for each histogram bin</li>
     * </ol>
     * 
     * <p><b>Scaling Algorithm:</b></p>
     * <ul>
     *   <li>X-axis: Bins are evenly distributed across the width</li>
     *   <li>Y-axis: Values are normalized to the maximum value in the histogram</li>
     *   <li>Lines are drawn from bottom (y=histHeight) to scaled value height</li>
     * </ul>
     * 
     * @param histWidth desired width of the histogram visualization in pixels
     * @param histHeight desired height of the histogram visualization in pixels
     * @param histogram OpenCV Mat containing histogram data in the first column
     * @return Mat containing the rendered histogram visualization
     */
    public Mat draw(int histWidth, int histHeight, Mat histogram) {
        Mat histImage = new Mat(histHeight, histWidth, 16, new Scalar(0, 0, 0, 0));
        // the histogram values are only on the first column of the histogram matrix
        double[] histValues = MatrixUtilities.getDoubleColumn(0, histogram);
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
