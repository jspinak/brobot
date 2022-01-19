package database.primitives.image;

import org.sikuli.script.Pattern;
import org.springframework.stereotype.Component;

/**
 * Helper functions for working with Images.
 */
@Component
public class ImageInfo {

    private ImagePatterns imagePatterns;

    public ImageInfo(ImagePatterns imagePatterns) {
        this.imagePatterns = imagePatterns;
    }

    public String getAbsolutePathOfFirstFile(Image image) {
        return imagePatterns.getFirstPattern(image).getFilename();
    }

    public String getAbsolutePath(Image image, int indexOfFilename) {
        if (indexOfFilename >= image.getImageNames().size()) return null;
        Pattern pattern = imagePatterns.getPatterns(image).get(indexOfFilename);
        return pattern.getFilename();
    }

    public int getWidthOfFirstFile(Image image) {
        return imagePatterns.getFirstPattern(image).getBImage().getWidth();
    }

    public int getHeightOfFirstFile(Image image) {
        return imagePatterns.getFirstPattern(image).getBImage().getHeight();
    }
}
