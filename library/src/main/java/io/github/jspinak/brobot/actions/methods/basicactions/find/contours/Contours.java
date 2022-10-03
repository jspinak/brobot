package io.github.jspinak.brobot.actions.methods.basicactions.find.pixelGroups;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.imageUtils.GetImage;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.Canny;

@Component
public class Contours {

    private GetImage getImage;

    public Contours(GetImage getImage) {
        this.getImage = getImage;
    }

    public void getContours(Region region) {
        findContours(getImage.getMatFromScreen(region), true);
    }

    //from the book OpenCV on the JavaVM
    public List<MatOfPoint> findContours(Mat image, boolean onBlank) {
        Mat imageBW = new Mat();
        Imgproc.cvtColor(image, imageBW, Imgproc.COLOR_BGR2GRAY);
        Canny(imageBW, imageBW, 100, 300, 3, true);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(imageBW, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        imwrite("contours.png", imageBW);
        return contours;
    }
}
