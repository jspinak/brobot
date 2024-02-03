package io.github.jspinak.brobot.imageUtils;

import org.bytedeco.opencv.opencv_core.Mat;
import org.sikuli.script.Image;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class ImageOps {

    public static byte[] getBytes(Image image) {
        return BufferedImageOps.toByteArray(image.get());
    }

    public static Image getImage(byte[] bytes) {
        return new Image(BufferedImageOps.fromByteArray(bytes));
    }

    public static Image getImage(Mat mat) {
        return new Image(BufferedImageOps.fromMat(mat));
    }

    /**
     * Returns the BGR representation as a JavaCV Mat.
     * If there is a conversion issue, an empty Mat is returned.
     * @return the image as a Mat.
     */
    public static Mat getMatBGR(Image image) {
        return getMatBGR(image.get());
    }

    public static Mat getMatBGR(BufferedImage bufferedImage) {
        Optional<Mat> matOptional = MatOps.bufferedImageToMat(bufferedImage);
        return matOptional.orElseGet(Mat::new);
    }

    /**
     * Returns the HSV representation as a JavaCV Mat.
     * If there is a conversion issue, an empty Mat is returned.
     * @return the image as a Mat.
     */
    public static Mat getMatHSV(Image image) {
        return getMatHSV(image.get());
    }

    public static Mat getMatHSV(BufferedImage bufferedImage) {
        Optional<Mat> matOptional = MatOps.bufferedImageToMat(bufferedImage);
        Mat HSVmat = new Mat();
        if (matOptional.isPresent()) HSVmat = MatOps.BGRtoHSV(matOptional.get());
        return HSVmat;
    }

    public static boolean isEmpty(Image image) {
        return image.get() == null;
    }
}
