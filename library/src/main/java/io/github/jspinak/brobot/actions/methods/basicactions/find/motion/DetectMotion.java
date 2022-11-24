package io.github.jspinak.brobot.actions.methods.basicactions.find.motion;

import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_core.absdiff;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Component
public class DetectMotion {

    /**
     * Detect movement between image1 and image2
     * @param image1 the first image
     * @param image2 the second image
     */
    public Mat getAbsdiff(Mat image1, Mat image2) {
        Mat gray1 = new Mat();
        Mat gray2 = new Mat();
        Report.println("just before BGR2GRAY");
        opencv_imgproc.cvtColor(image1, gray1, opencv_imgproc.COLOR_BGR2GRAY);
        opencv_imgproc.cvtColor(image2, gray2, opencv_imgproc.COLOR_BGR2GRAY);
        Mat gauss1 = new Mat();
        Mat gauss2 = new Mat();
        Report.println("just before GaussianBlur");
        GaussianBlur(gray1, gauss1, new Size(5,5), 0);
        GaussianBlur(gray2, gauss2, new Size(5,5), 0);
        Mat absDiff = new Mat();
        Report.println("just before absdiff");
        absdiff(gauss1, gauss2, absDiff);
        Mat kernel = new Mat(Mat.ones(5, 5, 1));
        //Mat dilated = new Mat();
        //Report.println("just before dilate");
        //dilate(absDiff, dilated, kernel); // dilating the Mat makes differences more noticeable and contour detection easier
        Mat binary = new Mat();
        Report.println("just before binary");
        //threshold(dilated, binary, 20, 255, THRESH_BINARY); // this removes pixels with small changes
        threshold(absDiff, binary, 50, 255, THRESH_BINARY); // this removes pixels with small changes
        return binary;
    }


}
