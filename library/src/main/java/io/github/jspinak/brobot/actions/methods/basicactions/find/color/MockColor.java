package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.GetImage;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MockColor {

    private GetImage getImage;

    public MockColor(GetImage getImage) {
        this.getImage = getImage;
    }

    /*
    Inserts the image at random places in a Mat of zeros of the same size as the region.
     */
    public Mat getMockMat(Image image, Region region) {
        Mat mat = Mat.zeros(region.h, region.w, CvType.CV_32F);
        Mat img = getImage.getMatFromFilename(BrobotSettings.packageName+"/"+image.getFirstFilename());
        int n = new Random().nextInt(10);
        for (int i=0; i<n; i++) {
            int row = new Random().nextInt(region.h);
            int col = new Random().nextInt(region.w);
            mat.put(row, col, img.get(0,0));
        }
        return mat;
    }
}
