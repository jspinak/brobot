package io.github.jspinak.brobot.actions.methods.basicactions.find.histogram;

import io.github.jspinak.brobot.imageUtils.GetImage;
import io.github.jspinak.brobot.reports.Report;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.calcHist;
import static org.opencv.imgproc.Imgproc.line;

/**
 * This class is a drawing board for learning about histograms in OpenCV.
 * It is currently not used in any production code.
 */
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
        Mat mat = getImage.getMatFromFilename(imageName, false);
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
    public Mat getHistogram(Mat imgBGR) {
        Mat imgHSV = new Mat(), imgHist = new Mat();
        Imgproc.cvtColor(imgBGR, imgHSV, Imgproc.COLOR_BGR2HSV);
        /*
        Hue varies from 0 to 179, saturation from 0 to 255.
        Here, only hue is used for the histogram, so only channel 0 is used.
         */
        float[] ranges = { 0, 180 };
        MatOfFloat rangeMat = new MatOfFloat(ranges);
        int[] channels = { 0 };
        MatOfInt channelMat = new MatOfInt(channels);
        int hBins = 180;
        int[] histSize = { hBins };
        MatOfInt histSizeMat = new MatOfInt(histSize);
        List<Mat> hsvBaseList = List.of(imgHSV);
        Imgproc.calcHist(hsvBaseList, channelMat, new Mat(), imgHist, histSizeMat, rangeMat, false);
        return imgHist;
    }

    public void showHistogram(Mat histogram, MatOfInt histSize) {
        Mat histImage = Mat.zeros(100, (int) histSize.get(0, 0)[0], CvType.CV_8UC1);

        histogram.convertTo(histogram, CvType.CV_32F);
        System.out.println("histImage size: " + histogram.size() +" channels: 2"+histogram.channels());

        // Normalize histogram
        Core.normalize(histogram, histogram, 1, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
        // Draw lines for histogram points
        for (int i = 0; i < (int) histSize.get(0, 0)[0]; i++) {
            line(histImage, new org.opencv.core.Point(i, histImage.rows()),
                    new org.opencv.core.Point(i, histImage.rows() - Math.round(histogram.get(i, 0)[0])),
                    new Scalar(255, 255, 255), 1, 8, 0);
        }
        imwrite("histogram.png", histImage);
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

    public Mat getRGBhistogram(String filename) {
        Mat img = getImage.getMatFromFilename(filename, false);
        Mat hist = new Mat(); // store the histogram here
        MatOfFloat ranges = new MatOfFloat(0, 256, 0, 256, 0, 256);
        MatOfInt histSize = new MatOfInt(256, 256, 256);
        calcHist(Collections.singletonList(img), new MatOfInt(3), new Mat(), hist, histSize, ranges);
        show(hist);
        return hist;
    }

    // check out
    // https://docs.opencv.org/3.1.0/d6/dc7/group__imgproc__hist.html#ga4b2b5fd75503ff9e6844cc4dcdaed35d
    // http://www.java2s.com/example/java-api/org/opencv/imgproc/imgproc/calchist-6-0.html
    public void show(Mat histogram) {
        MatOfInt histSize = new MatOfInt(255);
        // Create space for histogram image
        Mat histImage = Mat.zeros(100, (int) histSize.get(0, 0)[0], CvType.CV_8UC1);

        histogram.convertTo(histogram, CvType.CV_32F);

        // Normalize histogram
        Core.normalize(histogram, histogram, 1, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
        // Draw lines for histogram points
        for (int i = 0; i < (int) histSize.get(0, 0)[0]; i++) {
            line(histImage, new org.opencv.core.Point(i, histImage.rows()),
                    new org.opencv.core.Point(i, histImage.rows() - Math.round(histogram.get(i, 0)[0])),
                    new Scalar(255, 255, 255), 1, 8, 0);
        }
    }

    /**
     * Some examples of input types used in the OpenCV histogram function.
     */
    public void learn() {
        int[] histSize = { 3, 5 };
        MatOfInt histSizeM = new MatOfInt(histSize);
        printDescription("MatOfInt {3,5}", histSizeM);
        MatOfInt mat1 = new MatOfInt(3);
        printDescription("MatOfInt(3)", mat1);
        Mat mat2 = new Mat(3, 5, CvType.CV_8UC1);
        printDescription("Mat(3, 5, CvType.CV_8UC1)", mat2);
    }

    public void printDescription(String description, Mat mat) {
        Report.println(description);
        //Report.println(mat.dump());
        Report.println("size="+mat.size());
        Report.println("channels="+mat.channels());
    }

    public Mat learnHistogram(Mat mat) {
        Mat imgHSV = new Mat(), imgHist = new Mat();
        Imgproc.cvtColor(mat, imgHSV, Imgproc.COLOR_BGR2HSV);
        int hBins = 255, sBins = 60;
        int[] histSize = { hBins };
        MatOfInt histSizeM = new MatOfInt(histSize);

        // hue varies from 0 to 179, saturation from 0 to 255
        float[] ranges = { 0, 180 };
        // Use the 0-th and 1-st channels
        int[] channels = { 0, 1 };
        printDescription("histSizeM", histSizeM);
        printDescription("new MatOfFloat(ranges)", new MatOfFloat(ranges));
        List<Mat> hsvBaseList = List.of(imgHSV);
        Imgproc.calcHist(hsvBaseList, new MatOfInt(0), new Mat(), imgHist, histSizeM, new MatOfFloat(ranges), false);
        printDescription("imgHist", imgHist);

        // histImage holds the histogram graph
        System.out.println("hBins "+hBins);
        System.out.println((int) histSizeM.get(0, 0)[0]);
        Mat histImage = Mat.zeros(100, (int) histSizeM.get(0, 0)[0], CvType.CV_8UC1);
        printDescription("\nhistImage", histImage);
        imgHist.convertTo(imgHist, CvType.CV_32F);

        // Normalize histogram
        Core.normalize(imgHist, imgHist, 1, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
        printDescription("histImage", histImage);
        // Draw lines for histogram points
        int bottom = histImage.rows();
        long y;
        for (int i = 0; i < (int) histSizeM.get(0, 0)[0]; i++) {
            y = bottom - Math.round(imgHist.get(i, 0)[0]);
            System.out.print(i+"."+y+" ");
            line(histImage, new org.opencv.core.Point(i, bottom),
                    new org.opencv.core.Point(i, y),
                    new Scalar(255, 255, 255), 1, 8, 0);
        }
        imwrite("histogram.png", histImage);
        return imgHist;
    }

}

/*

// from book Building Computer Vision
void showHistoCallback(int state, void* userData)
{
// First, we need to create three matrices to process each input image channel. We use a vector-type variable to store each one and use the split OpenCV function to divide the input image among these three channels:
    // Separate image in BRG
    vector<Mat> bgr;
    split(img, bgr);

// Now, we are going to define the number of bins of our histogram, in our case, one per possible pixel value:
    // Create the histogram for 256 bins
    // The number of possibles values [0..255]
    int numbins= 256;

// Let's define our range of variables and create three matrices to store each histogram:
    /// Set the ranges for B,G,R last is not included
    float range[] = { 0, 256 } ;
    const float* histRange = { range };

    Mat b_hist, g_hist, r_hist;

/*
We can calculate the histograms using the calcHist OpenCV function. This function has several parameters with this order:

The input image: In our case, we use one image channel stored in the bgr vector
The number of images in the input to calculate the histogram: In our case, we only use 1 image
The number channel dimensions used to compute the histogram: We use 0 in our case
The optional mask matrix.
The variable to store the calculated histogram.
Histogram dimensionality: This is the dimension of the space where the image (here, a gray plane) is taking its values, in our case 1
Number of bins to calculate: In our case 256 bins, one per pixel value
Range of input variables: In our case, from 0 to 255 possible pixels values
 */
    /*
    calcHist(&bgr[0], 1, 0, Mat(), b_hist, 1, &numbins, &histRange);
    calcHist(&bgr[1], 1, 0, Mat(), g_hist, 1, &numbins, &histRange);
    calcHist(&bgr[2], 1, 0, Mat(), r_hist, 1, &numbins, &histRange);

    // Draw the histogram
    // We go to draw lines for each channel
    int width= 512;
    int height= 300;
    // Create image with gray base
    Mat histImage(height, width, CV_8UC3, Scalar(20,20,20));

    // Normalize the histograms to height of image
    normalize(b_hist, b_hist, 0, height, NORM_MINMAX);
    normalize(g_hist, g_hist, 0, height, NORM_MINMAX);
    normalize(r_hist, r_hist, 0, height, NORM_MINMAX);

    int binStep= cvRound((float)width/(float)numbins);
    for(int i=1; i< numbins; i++)
    {
        line(histImage,
                Point( binStep*(i-1), height-cvRound(b_hist.at<float>(i-1) )),
                Point( binStep*(i), height-cvRound(b_hist.at<float>(i) )),
                Scalar(255,0,0)
            );
        line(histImage,
                Point(binStep*(i-1), height-cvRound(g_hist.at<float>(i-1))),
                Point(binStep*(i), height-cvRound(g_hist.at<float>(i))),
                Scalar(0,255,0)
            );
        line(histImage,
                Point(binStep*(i-1), height-cvRound(r_hist.at<float>(i-1))),
                Point(binStep*(i), height-cvRound(r_hist.at<float>(i))),
                Scalar(0,0,255)
            );
    }

    imshow("Histogram", histImage);

}
 */