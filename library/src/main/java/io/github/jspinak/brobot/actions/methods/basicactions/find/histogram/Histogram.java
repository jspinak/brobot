package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.GetBufferedImage;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imread;


@Component
public class Histogram {

    private GetBufferedImage getBufferedImage;

    public Histogram(GetBufferedImage getBufferedImage) {
        this.getBufferedImage = getBufferedImage;
    }

    public Mat getHistogram(Region region) {
        return getHistogram(getBufferedImage.fromScreen(region));
    }

    /**
     * Returns a Mat with the histogram of the image. If the image cannot be read, the Mat will be empty.
     * @param imageName the name of the filename holding the image.
     * @return a Mat containing the histogram or an empty Mat.
     */
    public Mat getHistogram(String imageName) {
        Mat mat = getMat(imageName);
        if (mat.empty()) return mat;
        return getHistogram(mat);
    }

    public Mat getHistogram(BufferedImage bufferedImage) {
        return(getMat(bufferedImage));
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
        //Core.normalize(imgHist, imgHist, 0, 1, Core.NORM_MINMAX);
        return imgHist;
    }

    private Mat getMat(String imageName) {
        return imread(imageName); // reads a file and returns a Mat object.
    }

    /*
    the following 2 methods are from https://stackoverflow.com/questions/14958643/converting-bufferedimage-to-mat-in-opencv
     */
    public Mat getMat(BufferedImage image) {
        image = convertTo3ByteBGRType(image);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

    private BufferedImage convertTo3ByteBGRType(BufferedImage image) {
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        convertedImage.getGraphics().drawImage(image, 0, 0, null);
        return convertedImage;
    }

}
