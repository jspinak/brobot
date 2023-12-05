package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Component
public class PixelSimilarity {

    private final MatOps3d matOps3d;
    private final GetImageJavaCV getImage;
    private final Action action;
    private final MatVisualize matVisualize;

    public PixelSimilarity(MatOps3d matOps3d, GetImageJavaCV getImage, Action action,
                           MatVisualize matVisualize) {
        this.matOps3d = matOps3d;
        this.getImage = getImage;
        this.action = action;
        this.matVisualize = matVisualize;
    }

    /**
     * @param mat first Mat
     * @param comparisonMat second Mat
     * @return number of changed pixels in the first channel
     */
    public int getNumberOfChangedPixels(Mat mat, Mat comparisonMat) {
        Mat firstChannelOfMat = matOps3d.getFirstChannel(mat);
        Mat firstChannelOfComp = matOps3d.getFirstChannel(comparisonMat);
        Mat dist = new Mat(firstChannelOfMat.size(), firstChannelOfMat.type());
        absdiff(firstChannelOfMat, firstChannelOfComp, dist);
        return countNonZero(dist);
    }

    /**
     * Find the changed pixels with a mask, and return a new mask with only these pixels.
     * @param mat original Mat
     * @param comparisonMat Mat to compare
     * @param mask mask to use: only compare pixels turned on in the mask
     * @return a Mat of changed pixels from the pixels turned on in the mask
     */
    public Mat setChangedPixelsToMask(Mat mat, Mat comparisonMat, Mat mask) {
        Mat changedMat = getDynamicPixelMask(mat, comparisonMat); // finds non-matching pixels
        changedMat = matOps3d.bItwise_and(changedMat, mask);
        matVisualize.writeMatToHistory(changedMat, "changed");
        return changedMat;
    }

    /**
     * Get a mask of pixels that change values.
     * @param mat first Mat to compare
     * @param mat2 second Mat to compare
     * @return the mask of changed pixels.
     */
    public Mat getDynamicPixelMask(Mat mat, Mat mat2) {
        Mat mask = new Mat();
        matOps3d.cOmpare(mat, mat2, mask, CMP_NE); // find pixels with different values
        return mask;
    }

    public List<Region> getContoursAsRegions(Mat mat) {
        Mat oneChannel = matOps3d.getFirstChannel(mat);
        MatVector regionalContours = new MatVector();
        findContours(oneChannel, regionalContours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        List<Region> regions = new ArrayList<>();
        for (int i = 0; i < regionalContours.size(); i++) {
            Rect baseRect = boundingRect(regionalContours.get(i));
            Region region = new Region(baseRect.x(), baseRect.y(), baseRect.width(), baseRect.height());
            regions.add(region);
        }
        return regions;
    }


}
