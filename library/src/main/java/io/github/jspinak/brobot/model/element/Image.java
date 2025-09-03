package io.github.jspinak.brobot.model.element;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.github.jspinak.brobot.json.serializers.BufferedImageSerializer;
import io.github.jspinak.brobot.json.serializers.BufferedImageDeserializer;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import io.github.jspinak.brobot.util.image.core.ImageConverter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.Mat;
import java.awt.*;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;

/**
 * Physical representation of an image in the Brobot GUI automation framework.
 * 
 * <p>Image serves as the core container for visual data in Brobot, providing a unified 
 * interface for working with images across different formats and libraries. It acts as 
 * the bridge between Java's BufferedImage, OpenCV's Mat format, and SikuliX's image 
 * representation, enabling seamless interoperability.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Multi-format Support</b>: Stores images as BufferedImage internally with 
 *       conversions to Mat (BGR/HSV) and SikuliX formats</li>
 *   <li><b>Database Persistence</b>: Serializable to byte arrays for storage</li>
 *   <li><b>Color Space Conversions</b>: Built-in BGR and HSV representations for 
 *       advanced color-based matching</li>
 *   <li><b>Flexible Construction</b>: Can be created from files, BufferedImages, 
 *       Mats, Patterns, or SikuliX images</li>
 * </ul>
 * </p>
 * 
 * <p>Use cases in model-based automation:
 * <ul>
 *   <li>Storing screenshots captured during automation execution</li>
 *   <li>Holding pattern templates for visual matching</li>
 *   <li>Providing image data for color analysis and profiling</li>
 *   <li>Enabling image manipulation and processing operations</li>
 * </ul>
 * </p>
 * 
 * <p>The Image class abstracts away the complexity of working with multiple image 
 * libraries, providing a consistent API that supports the framework's cross-platform 
 * and technology-agnostic approach to GUI automation.</p>
 * 
 * @since 1.0
 * @see Pattern
 * @see BufferedImageUtilities
 * @see ImageConverter
 */
@Slf4j
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {

    private String name;

    @JsonSerialize(using = BufferedImageSerializer.class)
    @JsonDeserialize(using = BufferedImageDeserializer.class)
    private BufferedImage bufferedImage;

    public Image(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public Image(BufferedImage bufferedImage, String name) {
        this.bufferedImage = bufferedImage;
        this.name = name;
    }

    public Image(Mat BGRmat) {
        this.bufferedImage = BufferedImageUtilities.fromMat(BGRmat);
    }

    public Image(Mat BGRmat, String name) {
        this.bufferedImage = BufferedImageUtilities.fromMat(BGRmat);
        this.name = name;
    }

    public Image(String filename) {
        this.bufferedImage = BufferedImageUtilities.getBuffImgFromFile(filename);
        this.name = filename.replaceFirst("[.][^.]+$", ""); // the filename without extension
    }

    public Image(Pattern pattern) {
        this.bufferedImage = pattern.getBImage();
        this.name = pattern.getName();
    }

    public Image(org.sikuli.script.Image image) {
        this.bufferedImage = image.get();
    }

    /**
     * Returns the BGR representation as a JavaCV Mat.
     * If there is a conversion issue, an empty Mat is returned.
     * @return the image as a Mat.
     */
    @JsonIgnore
    public Mat getMatBGR() {
        if (bufferedImage == null) {
            log.error("[IMAGE] Cannot convert to Mat - bufferedImage is null for image: {}", name);
            return new Mat();
        }
        Mat mat = ImageConverter.getMatBGR(bufferedImage);
        if (mat == null || mat.empty()) {
            log.error("[IMAGE] Mat conversion failed for image: {}", name);
        } else {
            log.debug("[IMAGE] Successfully converted to Mat BGR - dimensions: {}x{} for image: {}", 
                    mat.cols(), mat.rows(), name);
        }
        return mat;
    }

    /**
     * Returns the HSV representation as a JavaCV Mat.
     * If there is a conversion issue, an empty Mat is returned.
     * @return the image as a Mat.
     */
    @JsonIgnore
    public Mat getMatHSV() {
        if (bufferedImage == null) {
            log.error("[IMAGE] Cannot convert to Mat - bufferedImage is null for image: {}", name);
            return new Mat();
        }
        Mat mat = ImageConverter.getMatHSV(bufferedImage);
        if (mat == null || mat.empty()) {
            log.error("[IMAGE] Mat HSV conversion failed for image: {}", name);
        } else {
            log.debug("[IMAGE] Successfully converted to Mat HSV - dimensions: {}x{} for image: {}", 
                    mat.cols(), mat.rows(), name);
        }
        return mat;
    }

    public boolean isEmpty() {
        return bufferedImage == null;
    }

    @JsonIgnore
    public org.sikuli.script.Image sikuli() {
        if (bufferedImage == null) {
            throw new IllegalStateException("Cannot create SikuliX Image: BufferedImage is null. " +
                "Image file may not exist or failed to load: " + name);
        }
        
        // VERSION 1.0.7 APPROACH - DEFAULT
        // Pass image directly to SikuliX without any conversion
        // This preserves exact pixel values and matches how patterns were captured with SikuliX tool
        // SikuliX handles any image type differences internally
        return new org.sikuli.script.Image(bufferedImage);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public int w() {
        if (bufferedImage == null) {
            log.warn("BufferedImage is null for image: {}", name);
            return 0;
        }
        return bufferedImage.getWidth();
    }

    public int h() {
        if (bufferedImage == null) {
            log.warn("BufferedImage is null for image: {}", name);
            return 0;
        }
        return bufferedImage.getHeight();
    }

    public static Image getEmptyImage() {
        Region r = new Region();
        BufferedImage bufferedImage = new BufferedImage(r.w(), r.h(), TYPE_BYTE_BINARY);
        Image image = new Image(bufferedImage);
        image.name = "empty scene";
        return image;
    }

    @Override
    public String toString() {
        return "Image{" +
                "name='" + name + '\'' +
                ", width=" + (bufferedImage != null ? bufferedImage.getWidth() : "N/A") +
                ", height=" + (bufferedImage != null ? bufferedImage.getHeight() : "N/A") +
                '}';
    }

}
