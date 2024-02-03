package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.opencv.highgui.HighGui.waitKey;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgproc.Imgproc.*;

// Consider using GetImageJavaCV for better compatibility.
@Component
public class GetImageOpenCV {

    public BufferedImage getBuffImgFromFile(String path) throws IOException {
        File f = new File(path);
        return ImageIO.read(Objects.requireNonNull(f));
    }

    public BufferedImage getBuffImgFromScreen(Region region) {
        return new Screen().capture(region.sikuli()).getImage();
    }

    public BufferedImage getScreenshot() {
        return new Screen().capture().getImage();
    }

    public Mat BGRtoHSV(Mat bgr) {
        Imgproc.cvtColor(bgr, bgr, Imgproc.COLOR_BGR2HSV);
        return bgr;
    }

    public Mat getMatFromFilename(String imageName, boolean hsv) {
        //if (hsv) return imread(imageName, COLOR_BGR2HSV); // this returns a 1x1 Mat for some strange reason. Mat [ 1*1*CV_8UC4 ...
        Mat mat = imread(imageName); // Mat [ 7*7*CV_8UC3 ...
        if (!hsv) return mat;
        return BGRtoHSV(mat);
    }

    public Mat getMatFromBundlePath(String imageName, boolean hsv) {
        return getMatFromFilename(ImagePath.getBundlePath()+"/"+imageName, hsv);
    }

    public Mat getMatFromScreen(Region region, boolean hsv) {
        Mat img = getMatFromScreen(region);
        if (!hsv) return img;
        return BGRtoHSV(img);
    }

    public Mat getMatFromScreen(Region region) {
        BufferedImage bi = getBuffImgFromScreen(region);
        return getMatFromBufferedImage(bi);
    }

    public Mat getMatFromScreen(boolean hsv) {
        return getMatFromScreen(new Region(), hsv);
    }

    public Mat getMatFromScreen() {
        return getMatFromScreen(new Region());
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
