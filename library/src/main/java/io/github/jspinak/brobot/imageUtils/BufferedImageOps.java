package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class BufferedImageOps {


    public static BufferedImage getBuffImgFromFile(String path) {
        File f = new File(FilenameOps.addPngExtensionIfNeeded(path));
        try {
            return ImageIO.read(Objects.requireNonNull(f));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BufferedImage getBuffImgFromScreen(Region region) {
        return new Screen().capture(region.sikuli()).getImage();
    }

    public List<BufferedImage> getBuffImgsFromScreen(List<Region> regions) {
        ScreenImage screenImage = new Screen().capture(); // uses IRobot
        List<BufferedImage> bufferedImages = new ArrayList<>();
        regions.forEach(region -> bufferedImages.add(
                screenImage.getSub(new Rectangle(region.x(), region.y(), region.w(), region.h())).getImage()));
        return bufferedImages;
    }

    public BufferedImage convertTo3ByteBGRType(BufferedImage image) {
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        convertedImage.getGraphics().drawImage(image, 0, 0, null);
        return convertedImage;
    }

    public BufferedImage convert(org.bytedeco.opencv.opencv_core.Mat mat) {
        Mat cvMat = MatOps.convertToOpenCVmat(mat);
        return convert(cvMat);
    }

    public BufferedImage convert(Mat mat) {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, mob);
        byte[] ba = mob.toArray();
        BufferedImage bi;
        try {
            bi = ImageIO.read(new ByteArrayInputStream(ba));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bi;
    }

    public static BufferedImage fromMat(org.bytedeco.opencv.opencv_core.Mat mat) {
        Mat cvMat = MatOps.convertToOpenCVmat(mat);
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", cvMat, mob);
        byte[] ba = mob.toArray();
        BufferedImage bi;
        try {
            bi = ImageIO.read(new ByteArrayInputStream(ba));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bi;
    }

    public static byte[] toByteArray(BufferedImage bufferedImage) {
        String format = "png";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, format, baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    public static BufferedImage fromByteArray(byte[] bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
