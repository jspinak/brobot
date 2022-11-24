package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import io.github.jspinak.brobot.imageUtils.MatOps3d;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2HSV;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

@Component
public class SetColorCluster {

    private MatOps3d matOps3d;

    public SetColorCluster(MatOps3d matOps3d) {
        this.matOps3d = matOps3d;
    }

    public ColorCluster getColorProfile(Mat oneColumnBGRMat) {
        return getColorProfile(oneColumnBGRMat, new Mat(oneColumnBGRMat.size(), oneColumnBGRMat.type(), new Scalar(255, 255, 255, 0))); // mask needs 3 channels
    }

    public ColorCluster getColorProfile(Mat oneColumnBGRMat, Mat mask) {
        ColorCluster colorCluster = new ColorCluster();
        ColorSchema colorSchemaBGR = getColorSchema(oneColumnBGRMat, mask, ColorCluster.ColorSchemaName.BGR);
        colorCluster.setSchema(ColorCluster.ColorSchemaName.BGR, colorSchemaBGR);
        Mat oneColumnHSVMat = new Mat();
        cvtColor(oneColumnBGRMat, oneColumnHSVMat, COLOR_BGR2HSV);
        ColorSchema colorSchemaHSV = getColorSchema(oneColumnHSVMat, mask, ColorCluster.ColorSchemaName.HSV);
        colorCluster.setSchema(ColorCluster.ColorSchemaName.HSV, colorSchemaHSV);
        return colorCluster;
    }

    /**
     * Create a ColorSchema for either BGR or HSV.
     * @param oneCol3ChanMat all pixels from all image files in a one column 3 channel Mat
     * @param masks The masks to use for each channel. These are important when getting a ColorSchema for
     *              a k-means cluster, because different pixels are considered for each cluster.
     */
    public ColorSchema getColorSchema(Mat oneCol3ChanMat, Mat masks, ColorCluster.ColorSchemaName schema) {
        // get the mean and stddev for each channel
        MatVector meanStddev = matOps3d.mEanStdDev(oneCol3ChanMat, masks);
        Mat means = meanStddev.get(0);
        Mat stddevs = meanStddev.get(1);
        // get the min and max for each channel
        DoublePointer min = new DoublePointer(3);
        DoublePointer max = new DoublePointer(3);
        matOps3d.minMax(oneCol3ChanMat, min, max, masks);
        ColorSchema colorSchema;
        if (schema == ColorCluster.ColorSchemaName.BGR) colorSchema = new ColorSchemaBGR();
        else colorSchema = new ColorSchemaHSV();
        for (int i=0; i<3; i++) {
            colorSchema.setValues(i, min.get(i), max.get(i),
                    means.createIndexer().getDouble(0,0,i),
                    stddevs.createIndexer().getDouble(0,0,i));
        }
        return colorSchema;
    }

}
