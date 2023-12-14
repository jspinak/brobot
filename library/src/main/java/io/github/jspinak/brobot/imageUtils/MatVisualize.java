package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorStatProfile;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.*;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorSchema.ColorValue.HUE;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorSchema.ColorValue.SATURATION;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * Mainly for testing, this class provides code to easily visualize Mats.
 * For example, a scores Mat can be shown in a specific color that varies in intensity based on the pixel score.
 */
@Component
public class MatVisualize {

    private final ImageUtils imageUtils;

    int uniqueNumber = 0; // used to make filenames unique

    public MatVisualize(ImageUtils imageUtils) {
        this.imageUtils = imageUtils;
    }

    /**
     * The color should be very bright when there is high confidence and very dark when there is low confidence.
     * For Mats representing scores for classification, low values represent high confidence.
     * Therefore, score is inverted onto the value channel, where low values are dark and high values are bright.
     *
     * @param toShow contains the pixel values to use
     * @param color Hue and Saturation won't be changed, Value has an inverse relationship to the pixel values
     * @param filename the filename to save the Mat to
     */
    public void writeScoresHSV(Mat toShow, ColorStatProfile color, String filename) {
        if (toShow == null || toShow.empty()) {
            Report.println("MatVisualize.writeHSV: toShow is null or empty for "+filename);
            return;
        }
        if (color == null) {
            Report.println("MatVisualize.writeHSV: color is null for "+filename);
            return;
        }
        MatVector toShowChannels = new MatVector(toShow.channels());
        color.print();
        Mat ch1 = new Mat(toShow.size(), CV_8UC1, Scalar.all(color.getStat(HUE)));
        Mat ch2 = new Mat(toShow.size(), CV_8UC1, Scalar.all(color.getStat(SATURATION)));
        Mat ch3; // this should be a 2d Mat with scores, but could also be 3d; in this case we take just the first channel
        if (toShow.channels() == 1) {
            ch3 = toShow;
        } else {
            split(toShow, toShowChannels);
            ch3 = toShowChannels.get(0);
        }
        // ch3 represents the score, so we want to invert it onto the value channel
        // this way, low scores are bright and high scores are dark
        bitwise_not(ch3, ch3);
        MatOps.info(ch1, "ch1");
        MatOps.info(ch2, "ch2");
        MatOps.info(ch3, "ch3");
        MatVector matVector = new MatVector(ch1, ch2, ch3);
        Mat hsv = new Mat();
        merge(matVector, hsv);
        hsv.convertTo(hsv, CV_32FC3);
        Mat bgr = new Mat();
        cvtColor(hsv, bgr, COLOR_HSV2BGR);
        imwrite("history/" + filename + new Random().nextInt(1000) + ".png", bgr); // write in BGR format
    }

    public Mat getHSVfromHue(Mat toShow) {
        if (toShow == null || toShow.empty()) {
            Report.println("MatVisualize.write2dHueMat: toShow is null or empty.");
            return null;
        }
        MatVector toShowChannels = new MatVector(toShow.channels());
        Mat ch1 = toShow;
        Mat ch2 = new Mat(toShow.size(), CV_8UC1, Scalar.all(255));
        Mat ch3 = new Mat(toShow.size(), CV_8UC1, Scalar.all(255));
        MatVector matVector = new MatVector(ch1, ch2, ch3);
        Mat hsv = new Mat();
        merge(matVector, hsv);
        return hsv;
    }

    public Mat getBGRfromBW(Mat toShow) {
        if (toShow == null || toShow.empty()) {
            Report.println("MatVisualize.write2dHueMat: toShow is null or empty.");
            return null;
        }
        MatVector toShowChannels = new MatVector(toShow.channels());
        Mat ch1 = toShow;
        Mat ch2 = toShow;
        Mat ch3 = toShow;
        MatVector matVector = new MatVector(ch1, ch2, ch3);
        Mat bgr = new Mat();
        merge(matVector, bgr);
        return bgr;
    }

    private Mat getBGRfromHue(Mat toShow) {
        Mat hsv = getHSVfromHue(toShow);
        hsv.convertTo(hsv, CV_32FC3);
        Mat bgr = new Mat();
        cvtColor(hsv, bgr, COLOR_HSV2BGR);
        return bgr;
    }

    /**
     * Meant for visually displaying a classification Mat (i.e. a 2d Mat with index values.)
     * The index values are converted into BGR colors for display.
     * Since no color as provided for this method,
     * they are chosen based on the number of different values to display.
     */
    public Mat getBGRColorMatFromHSV2dIndexMat(Mat indexMat) {
        List<Integer> indices = getIndicesExcluding0(indexMat);
        Map<Integer, Scalar> indexToColor = getHueMap(indices);
        return getBGRColorMatFromHSV2dIndexMat(indexMat, indexToColor);
    }

    /**
     * Meant for visually displaying a classification Mat (i.e. a 2d Mat with index values.)
     * The index values are converted into BGR colors for display.
     */
    public Mat getBGRColorMatFromHSV2dIndexMat(Mat indexMat, Map<Integer, Scalar> colors) {
        Mat indexCh1 = new Mat(indexMat.size(), CV_8UC1);
        if (indexMat.channels() > 1) indexCh1 = MatOps.getFirstChannel(indexMat);
        List<Integer> indices = getIndicesExcluding0(indexCh1);
        Mat colorMat = new Mat(indexCh1.size(), CV_8UC3, new Scalar(0, 0, 0, 0));
        for (int i = 0; i < indices.size(); i++) {
            int index = indices.get(i);
            if (colors.containsKey(index)) {
                Mat mask = new Mat();
                inRange(indexCh1, new Mat(new Scalar(index)), new Mat(new Scalar(index)), mask);
                Mat color = new Mat(indexCh1.size(), CV_8UC3, colors.get(index));
                color.copyTo(colorMat, mask);
            }
        }
        Mat bgrMat = new Mat();
        cvtColor(colorMat, bgrMat, COLOR_HSV2BGR);
        return bgrMat;
    }

    public void writeIndices(Mat mat, String filename, Map<Integer, Scalar> hues) {
        Report.print("hues = ");
        Report.println();
        Mat bgrMat = getBGRColorMatFromHSV2dIndexMat(mat, hues);
        imwrite("history/" + filename + new Random().nextInt(1000) + ".png", bgrMat);
    }

    public void writeIndices(Mat indexMat, String filename) {
        writeMatToHistory(getBGRColorMatFromHSV2dIndexMat(indexMat), filename);
    }

    /*
    max hue in opencv is 179, but the scale is circular, so we want to treat 179 like 0.
    therefore, max is 160.
    with 2 values, a hueStep of 80 would give us red (0) and green (80)
    with 10 values, with a step of 16, we would have from red (0) to hot pink (160)
     */
    private Map<Integer, Scalar> getHueMap(List<Integer> indicesWithout0) {
        if (indicesWithout0.size() == 0) {
            Report.println("MatVisualize getBGRMat...: Mat does not have valid index values.");
            return new HashMap<>();
        }
        int hueStep = 160 / indicesWithout0.size();
        Map<Integer, Scalar> hues = new HashMap<>();
        for (int i = 0; i < indicesWithout0.size(); i++) {
            hues.put(indicesWithout0.get(i), new Scalar(i * hueStep, 255, 255, 0));
        }
        return hues;
    }

    /**
     * A list of all indices except for 0, which corresponds to no class.
     * @param indicesMat a 2d Mat with index values
     * @return a list of all indices except for 0.
     */
    private List<Integer> getIndicesExcluding0(Mat indicesMat) {
        List<Integer> indices = new ArrayList<>();
        double[] minMax = MatOps.getMinMaxOfFirstChannel(indicesMat);
        if (minMax.length <= 1) {
            Report.println("MatVisualize getBGRMat...: min or max is null");
            return indices;
        }
        for (int i=(int)minMax[0]; i<=(int)minMax[1]; i++) {
            if (i != 0 && MatOps.firstChannelContains(indicesMat, i)) {
                indices.add(i);
            }
        }
        return indices;
    }

    public void writeMatToHistory(Mat mat, String filename) {
        String path = imageUtils.getFreePath(BrobotSettings.historyPath + filename);
        imwrite(path + ".png", mat);
    }

}
