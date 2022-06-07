package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2HSV;
import static org.opencv.imgproc.Imgproc.cvtColor;

@Component
public class GetImage {

    public BufferedImage getBuffImgFromFile(String path) throws IOException {
        File f = new File(path);
        return ImageIO.read(Objects.requireNonNull(f));
    }

    public BufferedImage getBuffImgFromScreen(Region region) {
        return new Screen().capture(region).getImage();
    }

    public Mat getMatFromFilename(String imageName) {
        new Pattern(); // make sure OpenCV is loaded (SikuliX does this)
        return imread(imageName); // reads a file and returns a Mat object.
    }

    public Mat getMatFromScreen(Region region) {
        BufferedImage bi = getBuffImgFromScreen(region);
        return getMatFromBufferedImage(bi);
    }

    /*
    the following 2 methods are from https://stackoverflow.com/questions/14958643/converting-bufferedimage-to-mat-in-opencv
    */
    public Mat getMatFromBufferedImage(BufferedImage image) {
        image = convertTo3ByteBGRType(image);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

    private BufferedImage convertTo3ByteBGRType(BufferedImage image) {
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        convertedImage.getGraphics().drawImage(image, 0, 0, null);
        return convertedImage;
    }

    public Mat convertToHSV(Mat mat) {
        Mat hsv = new Mat();
        cvtColor(mat, hsv, COLOR_BGR2HSV );
        return hsv;
    }
}
