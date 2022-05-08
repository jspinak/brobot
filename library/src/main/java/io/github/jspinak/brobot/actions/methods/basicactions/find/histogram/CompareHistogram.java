package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.reports.Report;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.*;

@Component
public class CompareHistogram {

    private Histogram histogram;

    public CompareHistogram(Histogram histogram) {
        this.histogram = histogram;
    }

    /**
     * The first filename of an Image is used to capture the histogram.
     * The compare method used for the OpenCV method is Correlation.
     * @param image1 The first Image to compare
     * @param image2 The second Image to compare
     * @return the correlation score of the two histograms. Return 0 if creating one of the histograms
     * fails.
     */
    public double compareHist(Image image1, Image image2) {
        Mat mat1 = histogram.getHistogram(image1.getFilenames().get(0));
        Mat mat2 = histogram.getHistogram(image2.getFilenames().get(0));
        if (mat1.empty() || mat2.empty()) return 0;
        return Imgproc.compareHist(mat1, mat2, 0);
    }

    /**
     * image1 and image2 need to be the same size
     * @param image1 the first image to compare
     * @param image2 the second image to compare
     * @return a double from 0.0 to 1.0
     */
    public double compareHist(BufferedImage image1, BufferedImage image2) {
        Mat mat1 = histogram.getHistogram(image1);
        Mat hist1 = new Mat();
        setHist(mat1, hist1);
        //mat1.convertTo(mat1, CV_32F);
        Mat mat2 = histogram.getHistogram(image2);
        Mat hist2 = new Mat();
        setHist(mat2, hist2);
        //mat2.convertTo(mat2, CV_32F);
        if (mat1.empty() || mat2.empty()) return 0;
        double result = Imgproc.compareHist(hist1, hist2, 0);
        //Report.print(" "+result);
        //if (new Random().nextInt(100) < 10) System.out.println(mat1);
        return result;
    }

    private Mat convertToHSV(Mat mat) {
        Mat hsv = new Mat();
        cvtColor(mat, hsv, COLOR_BGR2HSV );
        return hsv;
    }

    private void setHist(Mat mat, Mat hist) {
        int hbins = 50;
        int sbins = 60;
        MatOfInt histSize = new MatOfInt(hbins, sbins);
        // hue varies from 0 to 179, saturation from 0 to 255
        MatOfFloat histRange = new MatOfFloat(0f, 180f, 0f, 256f);
        Mat hsv = convertToHSV(mat);
        List<Mat> matList = new ArrayList<>();
        matList.add(hsv);
        calcHist(matList, new MatOfInt(0, 1), new Mat(), hist, histSize, histRange);
    }

}
