package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.springframework.stereotype.Component;
import java.util.Base64;

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

    /**
     * Creates a new SikuliX Pattern and retrieve the BufferedImage from the new Pattern.
     * @param path the filename of the image
     * @return the BufferedImage from file
     */
    public static BufferedImage getBuffImgFromFile(String path) {
        Pattern sikuliPattern = new Pattern(path);
        return sikuliPattern.getBImage();
    }

    /**
     * To use this method, you need to deal with paths. In SikuliX, there is a bundle path.
     * SikuliX has code to abstract the OS. If you want Brobot to be platform independent, it should use SikuliX
     * as much as possible to retrieve BufferedImages from file.
     * @param path the path of the image
     * @return the BufferedImage from an image on file
     */
    public static BufferedImage getBuffImgDirectly(String path) {
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

    public static BufferedImage getSubImage(BufferedImage originalImage, Region region) {
        return getSubImage(originalImage, region.x(), region.y(), region.w(), region.h());
    }

    public static BufferedImage getSubImage(BufferedImage originalImage, int x, int y, int width, int height) {
        // Resize the region to be within the bounds of the original image
        x = Math.max(0, x);
        y = Math.max(0, y);
        width = Math.min(originalImage.getWidth() - x, width);
        height = Math.min(originalImage.getHeight() - y, height);
        // Get the sub-image using getSubimage method
        return originalImage.getSubimage(x, y, width, height);
    }

    /**
     * Helper method to convert BufferedImage to Base64 String
     * @param image the BufferedImage to covert
     * @return a Base64 String representing the BufferedImage
     */
    public static String encodeImage(BufferedImage image) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bos);
            byte[] imageBytes = bos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage base64StringToImage(String base64String) {
        byte[] imageBytes = Base64.getDecoder().decode(base64String); // Decode Base64 String to byte array
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes); // Create ByteArrayInputStream from the byte array
        BufferedImage image = null; // Read image from ByteArrayInputStream and create BufferedImage
        try {
            image = ImageIO.read(bis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            bis.close(); // Close the ByteArrayInputStream
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return image;
    }

}
