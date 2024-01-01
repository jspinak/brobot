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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class GetBufferedImage {


    public BufferedImage getBuffImgFromFile(String path) throws IOException {
        File f = new File(path);
        return ImageIO.read(Objects.requireNonNull(f));
    }

    public BufferedImage getBuffImgFromScreen(Region region) {
        return new Screen().capture(region).getImage();
    }

    public List<BufferedImage> getBuffImgsFromScreen(List<Region> regions) {
        ScreenImage screenImage = new Screen().capture(); // uses IRobot
        List<BufferedImage> bufferedImages = new ArrayList<>();
        regions.forEach(region -> bufferedImages.add(
                screenImage.getSub(new Rectangle(region.x, region.y, region.w, region.h)).getImage()));
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

}
