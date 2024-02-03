package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.Random;

import static org.bytedeco.opencv.global.opencv_core.CV_32F;

@Component
public class MockColor {

    private GetImageJavaCV getImage;

    public MockColor(GetImageJavaCV getImage) {
        this.getImage = getImage;
    }

    /*
    Inserts the image at random places in a Mat of zeros of the same size as the region.
     */
    public Mat getMockMat(Pattern image, Region region) {
        Mat mat = new Mat(Mat.zeros(region.h(), region.w(), CV_32F));
        Mat img = getImage.getMatFromFilename(
                BrobotSettings.packageName+"/"+image.getImgpath(), ColorCluster.ColorSchemaName.BGR);
        int n = new Random().nextInt(10);
        for (int i=0; i<n; i++) {
            int row = new Random().nextInt(region.h());
            int col = new Random().nextInt(region.w());
            img.copyTo(mat.rowRange(0, row).colRange(0, col));
        }
        return mat;
    }
}
