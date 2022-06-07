package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

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
        Mat hist1 = histogram.getHistogram(image1.getFilenames().get(0));
        Mat hist2 = histogram.getHistogram(image2.getFilenames().get(0));
        if (hist1.empty() || hist2.empty()) return 0;
        return Imgproc.compareHist(hist1, hist2, 0);
    }

    /**
     * image1 and image2 need to be the same size
     * @param image1 the first image to compare
     * @param image2 the second image to compare
     * @return a double from 0.0 to 1.0
     */
    public double compareHist(BufferedImage image1, BufferedImage image2) {
        Mat hist1 = histogram.getHistogram(image1);
        Mat hist2 = histogram.getHistogram(image2);
        if (hist1.empty() || hist2.empty()) return 0;
        return Imgproc.compareHist(hist1, hist2, 0);
    }



}
