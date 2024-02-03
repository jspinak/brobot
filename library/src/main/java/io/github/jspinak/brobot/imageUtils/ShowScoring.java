package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorSchema;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorInfo.ColorStat.MEAN;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Component
public class ShowScoring {

    private MatVisualize matVisualize;

    private int size = 100;

    public ShowScoring(MatVisualize matVisualize) {
        this.matVisualize = matVisualize;
    }

    public Mat getMeanBGR(ColorSchema colorSchema) {
        Mat hsvMean = colorSchema.getMat(MEAN, new Size(size,size));
        MatOps.printPartOfMat(hsvMean, 5, 5,"hsvMean");
        List.of(colorSchema.getColorStats(MEAN)).forEach(dbl -> Report.println("mean: " + Arrays.toString(dbl)));
        MatOps.info(hsvMean, "hsvMean");
        cvtColor(hsvMean, hsvMean, COLOR_HSV2BGR);
        addText(hsvMean, "Mean");
        return hsvMean;
    }

    public Mat getPixelBGR(Mat hsvPixel) {
        Mat bgrPixel = new Mat();
        cvtColor(hsvPixel, bgrPixel, COLOR_HSV2BGR);
        Mat bgrPixelResized = new Mat();
        resize(bgrPixel, bgrPixelResized, new Size(size,size), 0, 0, INTER_AREA);
        MatOps.info(bgrPixel, "bgrPixel");
        addText(bgrPixelResized, "Pixel");
        return bgrPixelResized;
    }

    public void showPixelAndMean(Mat hsvPixel, ColorSchema colorSchema) {
        Mat bgrPixel = getPixelBGR(hsvPixel);
        Mat bgrMean = getMeanBGR(colorSchema);
        Mat pixMean = new MatBuilder()
            .addHorizontalSubmats(bgrPixel, bgrMean)
            .setSpaceBetween(5)
            .build();
        matVisualize.writeMatToHistory(pixMean, "pixelAndMean");
    }

    public void addText(Mat mat, String text) {
        int fontScale = 1;
        Scalar fontColor = new Scalar(255, 255, 255, 0);
        Point xy = new Point(0, mat.rows());
        putText(mat, text, xy, FONT_HERSHEY_SIMPLEX, fontScale, fontColor);
    }

    public void showPixelHSV(Mat hsvPixel, double score, double distBelowThreshold, double distToMean,
                             double meanH, double meanS, double meanV, double rangeMinH, double rangeMinS, double rangeMinV,
                             double rangeMaxH, double rangeMaxS, double rangeMaxV, double distOutsideRange) {

    }
}
