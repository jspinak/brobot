package io.github.jspinak.brobot.app.buildWithoutNames.screenTransitions;

import io.github.jspinak.brobot.actions.methods.basicactions.find.motion.FindDynamicPixels;
import io.github.jspinak.brobot.imageUtils.MatBuilder;
import io.github.jspinak.brobot.imageUtils.MatOps3d;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_core.CMP_NE;

/**
 * DecisionMat(s) are visual representations of information used to make decisions about the
 * state structure when building a state structure without names. The layers of information include:
 * 1. The underlying screen (only for reference)
 * 2. Dynamic pixels are colored black since they should not be used to make decisions. Dynamic pixels are
 *    those that change while in the same state.
 * 3. When attempting to transition to another state, the non-dynamic pixels are compared. If the dynamic
 *    regions are vastly different, the screen most likely correspond to different states. Since only non-dynamic
 *    pixels are compared, a fixed pixel on one screen compared to a dynamic pixel on another screen should always
 *    produce a mismatch. Pixels that don't match can be given a different color to mark them visually.
 */
@Component
public class DecisionMatBuilder {

    private final MatOps3d matOps3d;
    private final FindDynamicPixels findDynamicPixels;

    private int compareId;
    private Mat analysisMat;
    private int numberOfChangedPixels;
    private Mat combinedMats;

    public DecisionMatBuilder(MatOps3d matOps3d, FindDynamicPixels findDynamicPixels) {
        this.matOps3d = matOps3d;
        this.findDynamicPixels = findDynamicPixels;
    }

    /**
     * 1. Compare the dynamic pixel masks for both screens. Pixels that are dynamic in only one screen are marked
     *    as changed. The dynamic pixel mask for this class is then used to find additional changed pixels.
     * 2. Changed pixels are determined by comparing both screens, together with the dynamic pixel mask.
     * 3. All changed pixels are given the specified color.
     * @param compareId the id of the screen to which this images transitions
     * @param compareScreen the screenshot of the screen transitioned to
     * @param compareDynamicMask the dynamic pixel mask of the screen transitioned to
     * @return this class
     */
    public DecisionMatBuilder colorChangedPixels(Mat newScreen, Mat dynamicPixelMask, int compareId,
                                                 Mat compareScreen, Mat compareDynamicMask) {
        Scalar pink = new Scalar(147, 20, 255, 255);
        Scalar black = new Scalar(0, 0, 0, 255);
        this.compareId = compareId;
        analysisMat = newScreen.clone();
        /*
         cOmpare(,,CMP_NE) returns a 3D mask of changed pixels.
         FindDynamicPixels.getDynamicPixelMask() returns a 1D mask of changed pixels.
         All changed pixels in the analysis Mat are marked pink.
         */
        MatVector matVector = new MatVector(newScreen, compareScreen);
        Mat changedMask = findDynamicPixels.getDynamicPixelMask(matVector); //matOps3d.cOmpare(newScreen, compareScreen, CMP_NE);
        matOps3d.addColorToMat(analysisMat, changedMask, pink);
        // color all non-matching mask pixels pink
        Mat diffMask = matOps3d.cOmpare(dynamicPixelMask, compareDynamicMask, CMP_NE);
        matOps3d.addColorToMat(analysisMat, diffMask, pink);
        // pixels marked as dynamic in both masks get black
        Mat dynamic = matOps3d.bItwise_and(dynamicPixelMask, compareDynamicMask);
        matOps3d.addColorToMat(analysisMat, dynamic, black);
        Mat changedPixelsInDynamicMask = matOps3d.bItwise_and(changedMask, dynamicPixelMask);
        numberOfChangedPixels = matOps3d.getMaxNonZeroCellsByChannel(changedMask) -
                                matOps3d.getMaxNonZeroCellsByChannel(changedPixelsInDynamicMask);
        combineMats(newScreen, compareScreen, analysisMat);
        return this;
    }

    private void combineMats(Mat screenshot1, Mat screenshot2, Mat analysisMat) {
        combinedMats = new MatBuilder()
                .addHorizontalSubmats(screenshot1, screenshot2, analysisMat)
                .setSpaceBetween(4)
                .setName("combined")
                .build();
    }

    public DecisionMat build() {
        DecisionMat decisionMat = new DecisionMat();
        decisionMat.setAnalysis(analysisMat);
        decisionMat.setCombinedMats(combinedMats);
        decisionMat.setScreenComparedTo(compareId);
        decisionMat.setNumberOfChangedPixels(numberOfChangedPixels);
        return decisionMat;
    }
}
