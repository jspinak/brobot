package io.github.jspinak.brobot.actions.methods.basicactions.find.motion;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class ChangedPixels implements FindDynamicPixels {

    public Mat getDynamicPixelMask(MatVector matVector) {
        PixelChangeDetector pixelChangeDetector = new PixelChangeDetector.Builder()
                .setMats(matVector)
                .useGrayscale()
                .useThreshold(50, 255)
                .build();
        return pixelChangeDetector.getChangeMask();
    }
}
