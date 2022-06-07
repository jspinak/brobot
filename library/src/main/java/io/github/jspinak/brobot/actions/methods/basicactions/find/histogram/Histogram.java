package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.imageUtils.GetImage;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.calcHist;


@Component
public class Histogram {

    private GetImage getImage;

    public Histogram(GetImage getImage) {
        this.getImage = getImage;
    }

    /**
     * Returns a Mat with the histogram of the image. If the image cannot be read, the Mat will be empty.
     * @param imageName the name of the filename holding the image.
     * @return a Mat containing the histogram or an empty Mat.
     */
    public Mat getHistogram(String imageName) {
        Mat mat = getImage.getMatFromFilename(imageName);
        return getHistogram(mat);
    }

    public Mat getHistogram(BufferedImage bufferedImage) {
        Mat mat = getImage.getMatFromBufferedImage(bufferedImage);
        return getHistogram(mat);
    }

    /*
    Adapted from http://man.hubwiz.com/docset/OpenCV.docset/Contents/Resources/Documents/d8/dc8/tutorial_histogram_comparison.html

    Compare methods are:
    0. Correlation ( CV_COMP_CORREL )
    1. Chi-Square ( CV_COMP_CHISQR )
    2. Intersection ( method=CV_COMP_INTERSECT )
    3. Bhattacharyya distance ( CV_COMP_BHATTACHARYYA )
    */
    public Mat getHistogram(Mat mat) {
        if (mat.empty()) return mat;
        Mat imgHSV = new Mat(), imgHist = new Mat();
        Imgproc.cvtColor(mat, imgHSV, Imgproc.COLOR_BGR2HSV);
        int hBins = 50, sBins = 60;
        int[] histSize = { hBins, sBins };

        // hue varies from 0 to 179, saturation from 0 to 255
        float[] ranges = { 0, 180, 0, 256 };
        // Use the 0-th and 1-st channels
        int[] channels = { 0, 1 };

        List<Mat> hsvBaseList = List.of(imgHSV);
        Imgproc.calcHist(hsvBaseList, new MatOfInt(channels), new Mat(), imgHist, new MatOfInt(histSize), new MatOfFloat(ranges), false);
        return imgHist;
    }

    void setHist(Mat mat, Mat hist) {
        int hbins = 50;
        int sbins = 60;
        MatOfInt histSize = new MatOfInt(hbins, sbins);
        // hue varies from 0 to 179, saturation from 0 to 255
        MatOfFloat histRange = new MatOfFloat(0f, 180f, 0f, 256f);
        Mat hsv = getImage.convertToHSV(mat);
        List<Mat> matList = new ArrayList<>();
        matList.add(hsv);
        calcHist(matList, new MatOfInt(0, 1), new Mat(), hist, histSize, histRange);
    }

}
