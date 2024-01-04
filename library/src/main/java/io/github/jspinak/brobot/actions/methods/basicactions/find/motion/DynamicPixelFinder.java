package io.github.jspinak.brobot.actions.methods.basicactions.find.motion;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import io.github.jspinak.brobot.imageUtils.MatOps3d;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_core.CMP_NE;
import static org.bytedeco.opencv.global.opencv_core.bitwise_not;

@Component
public class DynamicPixelFinder implements FindDynamicPixels {

    private final MatOps3d matOps3d;
    private final GetImageJavaCV getImage;

    public DynamicPixelFinder(MatOps3d matOps3d, GetImageJavaCV getImage) {
        this.matOps3d = matOps3d;
        this.getImage = getImage;
    }

    /**
     * From a collection of Mat(s), returns a mask of pixels that change in any of the Mat(s).
     * @param matVector a collection of Mat objects
     * @return a 1D mask of changing pixels
     */
    public Mat getDynamicPixelMask(MatVector matVector) {
        int size = (int) matVector.size();
        if (size == 1) return new Mat(); // nothing to compare
        MatVector masks = new MatVector();
        Mat firstMat = matVector.get(0);
        for (int i=1; i<size; i++) {
            Mat dynamicPixels = matOps3d.cOmpare(firstMat, matVector.get(i), CMP_NE); // find pixels with different values
            masks.push_back(dynamicPixels);
        }
        Mat combinedMask = new Mat();
        for (Mat mask : masks.get()) {
            combinedMask = matOps3d.bItwise_or(mask, combinedMask);
        }
        return combinedMask;
    }

    public Mat getFixedPixelMask(MatVector matVector) {
        return matOps3d.bItwise_not(getDynamicPixelMask(matVector));
    }

    /**
     * Finds the pixels in a region that change over time.
     * @param region the region to search
     * @param intervalSeconds frequency of snapshots
     * @param totalSecondsToRun total seconds to observe the region
     * @return a mask of dynamic pixels
     */
    public Mat getDynamicPixelMask(Region region, double intervalSeconds, double totalSecondsToRun) {
        MatVector matVector = getImage.getMatsFromScreen(region, intervalSeconds, totalSecondsToRun);
        return getDynamicPixelMask(matVector);
    }

    /**
     * Finds the pixels in a region that over time do not change.
     * @param region the region to search
     * @param intervalSeconds frequency of snapshots
     * @param totalSecondsToRun total seconds to observe the region
     * @return a mask of dynamic pixels
     */
    public Mat getFixedPixelMask(Region region, double intervalSeconds, double totalSecondsToRun) {
        MatVector matVector = getImage.getMatsFromScreen(region, intervalSeconds, totalSecondsToRun);
        Mat dynamic = getDynamicPixelMask(matVector);
        Mat fixed = new Mat();
        bitwise_not(dynamic, fixed);
        return fixed;
    }

}
