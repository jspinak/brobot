package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import io.github.jspinak.brobot.imageUtils.MatOps3d;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2HSV;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

@Component
public class InitProfileMats {

    private final GetImageJavaCV getImage;
    private final MatOps3d matOps3d;

    public InitProfileMats(GetImageJavaCV getImage, MatOps3d matOps3d) {
        this.getImage = getImage;
        this.matOps3d = matOps3d;
    }

    /**
     * One column Mats are useful for Brobot Image objects since these objects can contain multiple files.
     * Each file can have a different size, and in order to process all of them together we create
     * a one column Mat and add each file to it.
     * @param stateImage StateImage to be processed.
     */
    public void setOneColumnMats(StateImage stateImage) {
        List<Mat> imgMatsBGR = getImage.getMats(stateImage, BGR);
        Mat oneColumnBGRMat = matOps3d.vConcatToSingleColumnPerChannel(imgMatsBGR);
        stateImage.setOneColumnBGRMat(oneColumnBGRMat);
        Mat oneColumnHSVMat = new Mat();
        cvtColor(stateImage.getOneColumnBGRMat(), oneColumnHSVMat, COLOR_BGR2HSV);
        stateImage.setOneColumnHSVMat(oneColumnHSVMat);
    }
}
