package io.github.jspinak.brobot.actions.methods.basicactions.find.motion;

import io.github.jspinak.brobot.imageUtils.MatOps3d;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class ChangedPixels implements FindDynamicPixels {

    private final MatOps3d matOps3d;

    public ChangedPixels(MatOps3d matOps3d) {
        this.matOps3d = matOps3d;
    }

    public Mat getDynamicPixelMask(MatVector matVector) {
        PixelChangeDetector pixelChangeDetector = new PixelChangeDetector.Builder()
                .setMats(matVector)
                .useGrayscale()
                .useThreshold(50, 255)
                .build();
        return pixelChangeDetector.getChangeMask();
    }

    public Mat getFixedPixelMask(MatVector matVector) {
        return matOps3d.bItwise_not(getDynamicPixelMask(matVector));
    }
}
