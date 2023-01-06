package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorInfo;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.MatOps;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static org.bytedeco.opencv.global.opencv_core.*;

@Component
public class GetDistanceMatrix {

    /**
     * Returns a Mat that has the per-cell absolute distance to a target color.
     * It can have multiple channels and each channel has a target value. (HSV and BGR are both 3d)
     * The target is usually a k-means center or the average color in an image. This is determined by the
     * parameter ColorStat.
     *
     * @param scene the Mat to analyze
     * @param colorCluster contains the target color
     * @param colorStat the stat to use (i.e. min, max, mean, stdDev)
     * @return a Mat with the per-cell absolute distance to the target
     */
    public Mat getAbsDist(Mat scene, ColorCluster.ColorSchemaName colorSchemaName,
                          ColorCluster colorCluster, ColorInfo.ColorStat colorStat) {
        Mat targetColor = colorCluster.getMat(colorSchemaName, colorStat, scene.size());
        targetColor.convertTo(targetColor, CV_64F); // convert to double; convertTo() does not change the # of channels
        Mat scene64 = scene.clone();
        scene64.convertTo(scene64, CV_64FC3);
        Mat dist = new Mat(scene.size(), CV_64FC3);
        absdiff(scene64, targetColor, dist);
        return dist;
    }

    public Mat getRelativeDist(Mat scene, ColorCluster.ColorSchemaName colorSchemaName,
                               ColorCluster colorCluster, ColorInfo.ColorStat colorStat) {
        Mat targetColor = colorCluster.getMat(colorSchemaName, colorStat, scene.size());
        Mat dist = new Mat(scene.size(), CV_64FC(scene.channels())); // CV_16SC(scene.channels())
        subtract(scene, targetColor, dist);
        return dist;
    }

    /**
     * Outside the search region, set cells in the Mat to a double.
     *
     * @param mat the Mat to modify
     * @param regionAsMask the search region as a mask
     * @param valueOutsideMask the value to set outside the search region
     * @return the Mat with the outside mask applied
     */
    public Mat setOutsideMask(Mat mat, Region regionAsMask, double valueOutsideMask) {
        Mat maskBase = new Mat(mat.size(), 0, new Scalar(valueOutsideMask)); // all cells 255
        Mat regionMask = new Mat(maskBase, regionAsMask.getJavaCVRect()); // maskBase with the searchRegion as roi
        bitwise_not(regionMask, regionMask); // now, maskBase has 255 everywhere outside the Mask
        bitwise_or(mat, regionMask, mat); // the values inside the mask are kept. the values outside are set to 255.
        return mat;
    }

    /**
     * Returns a Mat with the distance below the min and above the max for each cell.
     * Cells within the min and max are set to 0.
     *
     * @param scene the Mat to analyze
     * @param colorSchemaName the color schema to use
     * @param colorCluster the color profile to use
     * @return a Mat with the distance below the min and above the max for each cell
     */
    public Mat getDistanceBelowMinAndAboveMax(Mat scene, ColorCluster.ColorSchemaName colorSchemaName,
                                              ColorCluster colorCluster) {
        Mat targetColorMin = colorCluster.getMat(colorSchemaName, ColorInfo.ColorStat.MIN, scene.size());
        Mat distBelowMin = new Mat(scene.size(), CV_64FC(scene.channels()), new Scalar(255)); // CV_8UC(scene.channels())
        subtract(scene, targetColorMin, distBelowMin, new Mat(), 0);
        Mat targetColorMax = colorCluster.getMat(colorSchemaName, ColorInfo.ColorStat.MAX, scene.size());
        Mat distAboveMax = new Mat(scene.size(), CV_64FC(scene.channels()), new Scalar(255));
        subtract(targetColorMax, scene, distAboveMax, new Mat(), 0);
        Mat combinedDist = new Mat(scene.size(), CV_64FC(scene.channels()));
        add(distBelowMin, distAboveMax, combinedDist);
        return combinedDist;
    }

    /**
     * Calculates the distance to the nearest of the given colorStats. The distance can be negative in this
     * calculation, and the value with the nearest absolute distance will be kept. For example, if min and max
     * are used, and the min is 20 and max 60, a value of 10 would give min -10 and max -70. Since abs(-10) is
     * less than abs(-70), -10 will be kept.
     *
     * @param scene the Mat to analyze
     * @param colorCluster contains the target colors
     * @param colorStats the stats to use
     * @return a Mat with the directional distance to the nearest stat
     */
    public Mat getDistToNearest(Mat scene, ColorCluster.ColorSchemaName colorSchemaName,
                                ColorCluster colorCluster, ColorInfo.ColorStat... colorStats) {
        MatVector relDist = new MatVector();
        Arrays.stream(colorStats).forEach(cs -> relDist.put(getRelativeDist(scene, colorSchemaName, colorCluster, cs)));
        return minAbsAsOriginalValue(relDist);
    }

    private Mat minAbsAsOriginalValue(MatVector matVector) {
        if (matVector.empty()) return new Mat();
        Mat minAbsOrg = matVector.get(0).clone(); // this variable holds the original value of the minimum absolute value
        for (int i=1; i<matVector.size(); i++) {
            Mat abs1 = new Mat();
            absdiff(minAbsOrg, new Mat(new Scalar(0)), abs1); // absvalue
            Mat abs2 = new Mat();
            absdiff(matVector.get(i), new Mat(new Scalar(0)), abs2); // absvalue
            Mat abs2Smaller = new Mat();
            compare(abs2, abs1, abs2Smaller, CMP_LT); // abs2Larger is 255 where mat1 <= mat2
            minAbsOrg.setTo(abs2, abs2Smaller); // set the minAbsOrg to the value of abs2 where abs2Smaller is 255
        }
        return minAbsOrg;
    }

}
